# Migrating a template pack from Template Model 1 to Template Model 2

Template Model 2 ships with kibo 2.0.0. It renames the accessors that describe a
type **as the binding sees it**, and removes the ones that spelled a type for one
particular language inside the generator. There are no compatibility aliases: a
pack written against Model 1 does not render against kibo 2.0.

This guide is the complete list of what moved, and the rule that tells you when
you are done.

## The rule that makes this safe

**A Template Model migration does not change what your templates emit.** It
changes the names they read.

So the test is a fixed point:

```sh
# before, with your current kibo
java -jar kibo-1.2.x.jar -c <target> -n <ns> -d model.dsm.json -t templates/ -o before/

# after, with kibo 2.0 and your migrated templates
java -jar kibo-2.0.0.jar  -c <target> -n <ns> -d model.dsm.json -t templates/ -o after/

# every generated file names the jar that produced it, so that one line differs
diff -r -I 'by kibo-[0-9.]*\.jar' before/ after/     # must be empty
```

An empty diff means the migration is complete and correct. **A non-empty diff
means the migration is wrong**, not that the generator changed its mind — every
rename below maps one-to-one onto a name that returns the same string for the
same target. Run it on the largest model you have, not on a toy one: a small
model will not reach every accessor.

If your pack stamps its own version into what it generates, hold that stamp
fixed for the comparison, or ignore its line the same way.

If you also want to take up something Model 2 makes possible — dropping a leaf
table, say — do it as a *second* commit, after the fixed point holds. Otherwise a
diff tells you two things at once and you cannot read either.

## First: turn on the diagnostics, and read them

A template that reads an accessor the model does not carry renders the **empty
string**. The render succeeds, the file is written, and the missing part is
simply absent. Before 2.0.0 nothing at all was printed; that is why a rename
without aliases would have been undiagnosable.

From 2.0.0 every render reports what it could not resolve, on stderr, each
distinct diagnostic once with the number of times it fired:

```
kibo: templates/data.py.stg: context [/main /structure] 12:8 no such property or can't access: …
```

They are warnings — the file is still written and kibo still exits zero, because
whether the output is acceptable is your call. **A clean render prints nothing.**
During this migration, treat any line on stderr as a step not yet done.

## Does this affect your pack?

**A native target — C++ — is not affected.** Nothing it reads changed: `type`,
`typeInNamespace`, `elementType`, `keyType`, `passBy`, `isMovable`, `valueRef`,
`defaultValue`, the `*InNamespace` family, `viperValue`, `viperType`, `dsmType`,
`typeSuffix`, names, runtime ids, documentation. A C++ pack migrates with zero
edits and an empty diff — verified on a third-party `RaptorLogic` pack of 293
lines, rendered against a 366 KB model by kibo 1.2.11 and by kibo 2.0.0: the two
outputs differ only in the generator banner.

**A delegating target — one that reaches the runtime through a binding and
generates proxies — is affected.** Everything below applies to it.

## The renames

One-to-one. Each returns the same string as before, for the same target.

| Model 1 | Model 2 | On |
|---|---|---|
| `pythonType` | `bindingType` | the 17 classes that describe a type: concepts, clubs, enumerations, structures, structure fields, function parameters, attached key and document types, and the nine container functions |
| `pythonElementType` | `bindingElementType` | `vec`, `mat`, `optional`, `vector`, `set`, `map`, `xarray` functions, and `TemplateField` |
| `pythonKeyType` | `bindingKeyType` | `map` functions and `TemplateField` |
| `returnPythonType` | `returnBindingType` | functions and attachment functions |
| `pythonTupleType` | `bindingSequenceType` | `vec` and `mat` functions |
| `pythonColumnType` | `bindingColumnType` | `mat` functions |
| `pythonMembers` | `members` — see below | `tuple` and `variant` functions |

The members of the object itself are unchanged: `proxy`, `useProxy`,
`typeSuffix`, `type`.

A mechanical rewrite covers all but the last row. Do it, run the fixed point, and
read stderr.

## Three changes that are not renames

**`bindingType.type` answers for the target you are generating.** In Model 1 the
scalar `getType()` returned a Python spelling whatever the target: `int`, `str`,
`None`, `dsviper.ValueBlob`. In Model 2 it returns the spelling of the binding
`--converter` names — `int` under `python`, `bigint` under `typescript` for the
same 64-bit integer. If you generate with `-c python`, you get exactly what you
got before and the fixed point holds. If your pack targets another binding and
carried its own lookup table for this, see *Dropping your leaf table* below.

**`members` replaces `pythonMembers`, and carries more.** A tuple or variant
member is now one object describing all three spaces:

| Read | For |
|---|---|
| `.dsmType` | what the model calls the member — `uint8`, `Test::StructureS` |
| `.type` | the native spelling |
| `.bindingType` | the binding view: `.proxy`, `.useProxy`, `.type` |
| `.typeSuffix` | the neutral key naming the generated symbol |

So `<v.pythonMembers:{m|<m.type>}>` becomes `<v.members:{m|<m.bindingType.type>}>`,
and a member's own name for a message or a comment is `<m.dsmType>`. There is one
member list now, not two running in parallel.

**Entities carry `getDsmType()`.** A concept, a club, an enumeration and a
structure now answer `dsmType` with the name the model gives them —
`Test::ConceptA`, not the target's spelling of it. This is additive: nothing
required you to use it. It matters for the next section.

## Which space to name where

Model 2 makes a distinction the model could not express before, and it is worth
adopting even though nothing forces you to.

**In a type position** — a signature, an annotation, a declaration — use the
target's spelling: `bindingType.type`, or `bindingType.proxy` where you are
building a *name* rather than writing a type.

**In a comment, a docstring, a `repr` or an exception message** — use `dsmType`.
The DSM name carries the semantics; the proxy class name is only this binding's
implementation of it. Every such message in a generated file guards a runtime
type comparison, and a runtime type *is* a DSM type. The DSM name is also what
whoever wrote the model will recognise, and it reads the same whatever the
target.

Adopting this **will** move your generated output, so do it after the fixed point
holds, as its own change.

## Dropping your leaf table

If your pack carries a dictionary mapping DSM primitive names to your binding's
spellings — `int64` to `bigint`, `blob` to a runtime value class — Model 2 lets
you delete it and read `bindingType.type` instead, provided kibo knows your
binding. The first-party packs did exactly this.

kibo 2.0 ships vocabularies for `python` and `typescript`. If yours is neither,
keep your table: `proxy` and `useProxy` are unchanged and still let you resolve a
leaf yourself. Adding a vocabulary is a small change to the generator — ask.

Either way, verify by the same fixed point. A table that disagreed with the
vocabulary in even one entry will show up as a diff.

## Checklist

1. Regenerate with your current kibo into `before/`.
2. Apply the renames in the table.
3. Rewrite `pythonMembers` to `members`, reading `.bindingType.type` where you
   read `.type`.
4. Regenerate with kibo 2.0 into `after/`.
5. `diff -r before/ after/` — empty.
6. stderr — empty.
7. Only then, take up `dsmType` in messages and comments, or drop your leaf
   table, each as its own change with its own diff.
