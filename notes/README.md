# Notes

This directory is for raw insights, research notes, and learnings that don't fit neatly into a system design, bug case, or post-mortem.

## What Goes Here

- Deep dives into a specific technology (e.g., "How PostgreSQL MVCC works")
- Summaries of papers or books (e.g., "Designing Data-Intensive Applications — key takeaways")
- Insights from production incidents at other companies (public post-mortems)
- Quick references you find yourself looking up repeatedly
- "Things I got wrong" reflections
- Interview prep notes (system design, behavioral)

## What Does NOT Go Here

- System design sessions → `system-design/`
- Bug analyses → `bug-cases/`
- Incident post-mortems → `post-mortems/`
- Reusable patterns → `patterns/`

## Naming Convention

Use descriptive, lowercase filenames with hyphens:

```
notes/
├── postgres-mvcc-deep-dive.md
├── kafka-consumer-groups-explained.md
├── ddia-key-takeaways.md
└── things-i-got-wrong-2024.md
```

## Note Quality Bar

Notes should be useful to your future self 6 months from now. Before saving:

- Is the core insight clearly stated?
- Would I understand this without context?
- Did I include a source or reference?
