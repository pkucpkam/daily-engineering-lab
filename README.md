# Daily Engineering Practice Lab

> A structured daily practice environment for thinking like a senior backend engineer.  
> Focus on real problems, real trade-offs, and production-grade reasoning.

---

## Purpose

This repository is a personal engineering practice lab. It exists to build and maintain the habits of a strong backend engineer through deliberate, daily practice:

- **System Design** — Practice designing real distributed systems end-to-end, from requirements to trade-offs.
- **Bug Analysis** — Practice methodical debugging: from symptom → hypothesis → root cause → fix → prevention.
- **Post-Mortem Writing** — Practice clear incident documentation: what happened, why, and how to prevent recurrence.
- **Engineering Patterns** — Capture reusable architectural and operational patterns worth remembering.
- **Notes** — Capture insights, learnings, and references worth revisiting.

This is not a course. It is not a collection of theory. It is a workspace for doing the thinking.

---

## Repository Structure

```
daily-engineering-lab/
├── README.md
│
├── system-design/
│   ├── templates/
│   │   └── system-design-template.md   ← Start here for every design session
│   └── [YYYY-MM-DD]-[topic]/           ← One folder per practice session
│       └── design.md
│
├── bug-cases/
│   ├── templates/
│   │   └── bug-template.md             ← Start here for every bug analysis
│   └── [YYYY-MM-DD]-[bug-description].md
│
├── post-mortems/
│   ├── templates/
│   │   └── post-mortem-template.md     ← Start here for every post-mortem
│   └── [YYYY-MM-DD]-[incident-title].md
│
├── patterns/
│   └── [pattern-name].md               ← Architectural/operational patterns
│
└── notes/
    └── [topic].md                      ← Raw insights, references, learnings
```

---

## How to Use This Repo Daily

### Daily Practice Workflow

**Step 1 — Pick a topic** (5 min)  
Choose one of:
- A system design problem (e.g., "Design a rate limiter", "Design a notification system")
- A real bug you've seen or read about
- A production incident post-mortem (real or synthetic)
- A pattern worth understanding deeply

**Step 2 — Think first, template second** (15–30 min)  
Before copying a template, spend 10–15 minutes writing your raw thinking in plain text. What's hard about this? What would break? What would you do first? Write it out.

**Step 3 — Fill the template** (30–60 min)  
Copy the relevant template. Fill every section. Don't leave sections blank — if you don't know the answer, write "I don't know yet, because…" and reason through it.

**Step 4 — Identify gaps** (10 min)  
At the end, list 2–3 things you were uncertain about. These become tomorrow's research.

**Step 5 — Commit and tag** (5 min)  
Commit your work. Use a meaningful filename with the date:  
`system-design/2024-01-15-design-a-url-shortener/design.md`

### Suggested Weekly Cadence

| Day       | Practice Focus                            |
|-----------|-------------------------------------------|
| Monday    | System Design — new problem               |
| Tuesday   | Bug Analysis — real bug from work or news |
| Wednesday | Pattern deep-dive (caching, queues, etc.) |
| Thursday  | System Design — revisit / improve Monday  |
| Friday    | Post-Mortem — write or analyze one        |

---

## Learning Goals

By consistently using this repo, you should develop:

1. **Requirements discipline** — The habit of clarifying functional/non-functional requirements before designing anything.
2. **Bottleneck intuition** — The ability to identify where a system will break before building it.
3. **Trade-off articulation** — The ability to explain *why* you chose one approach over alternatives.
4. **Debugging methodology** — A structured, hypothesis-driven approach to finding root causes.
5. **Incident communication** — The ability to write clear, blameless, actionable post-mortems.
6. **Pattern recognition** — A library of reusable solutions to common distributed systems problems.

---

## Rules

These rules exist because learning comes from reasoning, not from recalling or copy-pasting.

1. **Reason first, template second.** Write your raw thinking before touching any template. If you can't produce raw thinking, you haven't thought enough yet.

2. **No copy-paste from existing solutions.** If you've seen a similar system design before, use it as a starting point for your thinking — but your written design must be your own reasoning, not a transcription.

3. **No empty sections.** Every section in a template must be filled. If you don't know the answer, write what you *do* know, and articulate what you'd need to find out and why.

4. **Quantify everything.** Avoid vague statements. "High traffic" → "10,000 requests/second". "Slow query" → "p99 latency of 4.2 seconds". Precision is a habit.

5. **Every trade-off must be justified.** Don't just state what you chose. State what you rejected and why. Design decisions without rejected alternatives are not real decisions.

6. **Commit after every session.** Even rough work. Especially rough work. Progress compounds.

7. **Review your old work.** Once a month, re-read a past entry. Would you design it differently now? Update it.

---

## Getting Started

1. Clone this repo
2. Pick a system design problem or bug to analyze
3. Copy the relevant template to the correct folder with today's date in the filename
4. Follow the workflow above
5. Commit your work

### Quick Start Commands

```bash
# Start a new system design session
cp system-design/templates/system-design-template.md \
   system-design/$(date +%Y-%m-%d)-[your-topic]/design.md

# Start a new bug case
cp bug-cases/templates/bug-template.md \
   bug-cases/$(date +%Y-%m-%d)-[bug-description].md

# Start a new post-mortem
cp post-mortems/templates/post-mortem-template.md \
   post-mortems/$(date +%Y-%m-%d)-[incident-title].md
```

---

## Good Practice Topics to Start With

### System Design
- Design a URL shortener (Bitly)
- Design a rate limiter
- Design a notification system (push/email/SMS)
- Design a distributed job scheduler
- Design a real-time leaderboard
- Design a distributed cache (Redis-lite)
- Design an e-commerce checkout system

### Bug Analysis
- N+1 query problem causing API latency spike
- Memory leak in a long-running service
- Cascading timeout failures across microservices
- Race condition in distributed locking
- Cache stampede taking down the database

### Post-Mortems
- Database failover that took 40 minutes instead of 90 seconds
- Deployment that caused 100% error rate due to missing env variable
- Third-party payment provider outage and how your system responded

---

*Built for engineers who want to get better every day. The reps compound.*
