# MVP Scope & Tech Stack Deep Dive

## Quick Reference: 3 Top Ideas for You

| Idea | Timeline | Team Size | Tech Stack | Profit Path | Complexity |
|------|----------|-----------|-----------|------------|-----------|
| #1: AI Medical Document Analyzer | 2-3 months | 1-2 people | Python FastAPI + Chroma + Stripe | $5K-20K MRR in 6 mo | Medium |
| #2: Vertical AI Writer (Medical) | 1-2 months | 1 person | Python FastAPI + OpenAI API + Supabase | $2K-10K MRR in 6 mo | Low |
| #7: AI Code Review SaaS | 1-2 months | 1 person | Python FastAPI + GitHub API + Claude API | $5K-30K MRR in 6 mo | Low |

---

# Idea #1: AI-Powered Medical Document Analyzer (MOST RECOMMENDED FOR YOU)

## MVP Scope Definition (Using MoSCoW Method)

### MUST-HAVE (Core MVP)
- [ ] PDF upload and parsing (OCR with Pytesseract or PyPDF)
- [ ] Key medical data extraction (patient name, diagnosis, medications, dates)
- [ ] Vector embedding + semantic search using Chroma
- [ ] AI summary generation (using Claude API or open-source Llama)
- [ ] Simple web dashboard (upload → results display)
- [ ] User authentication (Google OAuth or email/password)
- [ ] Stripe subscription payment ($50-100/mo)
- [ ] API key system for future B2B access

### SHOULD-HAVE (Post-MVP, First Iteration)
- [ ] Multiple file format support (DOCX, images)
- [ ] Batch file processing (upload 10+ at once)
- [ ] Export results as PDF/CSV
- [ ] Basic role-based access (viewer, uploader, admin)
- [ ] Usage analytics dashboard
- [ ] Email alerts for processing completion

### COULD-HAVE (Later Phases)
- [ ] Custom medical templates (specialty-specific extraction)
- [ ] HIPAA compliance audit trail
- [ ] Integration with EHR systems (Epic, Cerner APIs)
- [ ] Multi-language support
- [ ] Mobile app

### WON'T-HAVE (Out of scope)
- [ ] Real-time document collaboration
- [ ] Video analysis
- [ ] Full EMR/EHR replacement

## Tech Stack for MVP

### Backend
```
Framework:      FastAPI (async, type-safe, auto-docs)
Python:         3.10+
Database:       PostgreSQL (user data, billing records)
Vector DB:      Chroma (local embedded for MVP, 0 ops)
LLM:            OpenAI API (GPT-4-turbo) or Anthropic Claude API
                Fallback: Ollama (local Llama 2-7B for cost control)
OCR:            Tesseract + Pytesseract (free, open-source)
File Processing: PyPDF2 + pdf2image (free)
Authentication: JWT + Google OAuth (python-jose + pydantic-oauth)
Payment:        Stripe (Stripe Python SDK)
Task Queue:     Celery + Redis (async PDF processing)
Email:          SendGrid API
Caching:        Redis (session cache, rate limiting)
Deployment:     Docker + Docker Compose (dev); AWS Fargate or Heroku (prod)
```

### Frontend
```
Framework:      React 18 or Vue 3 (simple dashboard)
UI Library:     Shadcn/UI or Bootstrap (rapid development)
Auth:           NextAuth.js or Auth0 (social login)
Payments:       Stripe.js (embedded checkout)
Charts:         Recharts or Chart.js (usage analytics)
Deploy:         Vercel (free tier for MVP)
```

### Infrastructure & DevOps
```
Development:    Python venv + Docker Compose
Database Host:  Supabase or AWS RDS (PostgreSQL)
Vector DB:      Chroma (embedded in Docker, or beta Chroma Cloud $20/mo)
LLM API:        OpenAI (pay-per-use: ~$0.02-0.05 per medical document)
Storage:        S3 or Supabase (uploaded PDFs, 50GB free tier)
CI/CD:          GitHub Actions (free)
Monitoring:     Sentry (error tracking), Mixpanel (analytics)
```

## MVP Implementation Timeline

| Phase | Week | Focus | Deliverable |
|-------|------|-------|------------|
| **Setup** | 1 | Project structure, FastAPI boilerplate, Docker setup | GitHub repo + local dev running |
| **Core Pipeline** | 2-3 | PDF parsing, OCR, vector embedding with Chroma | Upload → extract → embed working locally |
| **LLM Integration** | 4 | API calls to Claude/GPT-4, prompt engineering | Summaries generating |
| **Web UI** | 5 | React dashboard, file upload, results display | Simple working UI |
| **Auth & Payment** | 6 | Google OAuth, Stripe subscription | Can sign up + test payment |
| **Deploy & Polish** | 7-8 | Deploy to Heroku/AWS, UI polish, bugfixes, load testing | Public MVP live |
| **Validation** | 9-10 | 5-10 beta users, gather feedback, iterate | User feedback → roadmap |

## Deployment Strategy

### Development (Local)
```bash
# Uses Docker Compose
docker-compose up -d
# Services: FastAPI, PostgreSQL, Redis, Chroma all in containers
```

### Production (MVP Launch)
```
Option A: Heroku (easiest, $7-50/mo)
- Heroku Dyno for FastAPI
- Heroku Postgres add-on
- Heroku Redis add-on
- GitHub auto-deploy

Option B: AWS (more control, ~$30-80/mo)
- Fargate for FastAPI (serverless containers)
- RDS for PostgreSQL
- ElastiCache for Redis
- S3 for PDFs
- CloudFront for CDN

Option C: DigitalOcean (balance, ~$12-24/mo)
- App Platform for FastAPI
- Managed Database
- Managed Redis
```

## Sample Project Structure

```
medical-doc-analyzer/
├── backend/
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py                  # FastAPI app entry
│   │   ├── config.py                # Settings, env vars
│   │   ├── core/
│   │   │   ├── auth.py              # JWT + OAuth logic
│   │   │   ├── security.py          # Password hashing, etc
│   │   │   └── config.py
│   │   ├── api/
│   │   │   ├── v1/
│   │   │   │   ├── auth.py          # Login, signup routes
│   │   │   │   ├── documents.py     # Upload, extract routes
│   │   │   │   ├── subscription.py  # Stripe webhook
│   │   │   │   └── health.py        # Health check
│   │   ├── services/
│   │   │   ├── pdf_processor.py     # OCR + parsing
│   │   │   ├── embedding_service.py # Chroma integration
│   │   │   ├── llm_service.py       # OpenAI/Claude calls
│   │   │   └── payment_service.py   # Stripe logic
│   │   ├── models/
│   │   │   ├── user.py              # User SQLAlchemy model
│   │   │   ├── document.py          # Document model
│   │   │   └── subscription.py      # Subscription model
│   │   ├── schemas/
│   │   │   ├── user.py              # Pydantic schemas
│   │   │   ├── document.py
│   │   │   └── extraction.py
│   │   ├── tasks/
│   │   │   └── celery_tasks.py      # Async PDF processing
│   │   └── utils/
│   │       ├── logger.py
│   │       └── exceptions.py
│   ├── tests/
│   │   ├── test_auth.py
│   │   ├── test_pdf_processing.py
│   │   └── test_extraction.py
│   ├── requirements.txt
│   ├── Dockerfile
│   └── alembic/                     # DB migrations
├── frontend/
│   ├── pages/
│   │   ├── login.tsx
│   │   ├── dashboard.tsx
│   │   ├── upload.tsx
│   │   └── results.tsx
│   ├── components/
│   │   ├── FileUpload.tsx
│   │   ├── ResultsDisplay.tsx
│   │   └── PaymentModal.tsx
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   └── useDocuments.ts
│   ├── services/
│   │   └── api.ts                   # Axios instance
│   ├── package.json
│   └── .env.example
├── docker-compose.yml
├── .github/
│   └── workflows/
│       └── deploy.yml               # Auto-deploy on push
├── README.md
└── .env.example
```

## Key Implementation Details

### PDF Processing Pipeline
```python
# Pseudo-code for core pipeline

@router.post("/documents/upload")
async def upload_document(file: UploadFile, current_user: User = Depends(get_current_user)):
    # 1. Save file temporarily
    temp_path = await save_temp_file(file)
    
    # 2. Extract text via OCR (async task)
    task = process_pdf_async.delay(temp_path, current_user.id)
    
    return {"task_id": task.id, "status": "processing"}

# Celery async task
@celery_app.task
def process_pdf_async(file_path: str, user_id: int):
    # 1. OCR with Tesseract
    text = extract_text_from_pdf(file_path)
    
    # 2. Split into chunks
    chunks = split_text_into_chunks(text, chunk_size=512)
    
    # 3. Embed with Chroma
    embeddings = generate_embeddings(chunks)  # Using OpenAI Embedding API
    chroma_collection.add(
        ids=[f"{user_id}_{i}" for i in range(len(chunks))],
        embeddings=embeddings,
        metadatas=[{"user_id": user_id, "source": file.filename}],
        documents=chunks
    )
    
    # 4. Generate summary with LLM
    summary = llm_service.summarize_document(text)
    
    # 5. Extract key fields (Claude function calling)
    extraction = llm_service.extract_medical_fields(text)
    
    # 6. Store results in DB
    doc_record = Document(
        user_id=user_id,
        filename=file.filename,
        summary=summary,
        extracted_data=extraction,
        status="completed"
    )
    db.add(doc_record)
    db.commit()
```

### Vector Database (Chroma) Setup
```python
# MVP: Local embedded Chroma (0 infrastructure cost)
import chromadb

chroma_client = chromadb.Client()  # Local in-process

# Create collection per user (privacy boundary)
def get_user_collection(user_id: int):
    return chroma_client.get_or_create_collection(
        name=f"user_{user_id}",
        metadata={"hnsw:space": "cosine"}  # HNSW for semantic search
    )

# Semantic search over past documents
def search_documents(user_id: int, query: str, top_k: int = 5):
    collection = get_user_collection(user_id)
    results = collection.query(
        query_texts=[query],
        n_results=top_k
    )
    return results
```

## Cost Breakdown (Monthly, MVP Phase)

| Service | Cost | Notes |
|---------|------|-------|
| Heroku Dyno | $7-50 | Depends on traffic; start with $7 (512MB) |
| PostgreSQL | $9 | Heroku Postgres Standard |
| Redis | $15 | Heroku Redis |
| OpenAI API | $50-200 | ~$0.03 per document at $100/mo revenue |
| Stripe | 2.9% + $0.30 | Percentage of subscription revenue |
| Domain | $10 | Custom domain (optional for MVP) |
| SendGrid Email | Free | Up to 100/day free tier |
| **Total** | **~$100-150** | Break-even at ~2-3 paying customers |

## Revenue Model (Year 1 Projections)

| Month | Customers | MRR | Notes |
|-------|-----------|-----|-------|
| Month 1 | 0 | $0 | Launch, 5 beta users (free) |
| Month 2 | 2 | $100 | Friend referrals |
| Month 3 | 5 | $250 | Product Hunt launch |
| Month 6 | 15 | $750 | Google SEO + word-of-mouth |
| Month 12 | 50+ | $3K+ | B2B clinic pilots starting |

---

# Idea #2: Vertical AI Writer (Medical Case Summaries)

## MVP Scope (Much Simpler!)

### MUST-HAVE
- [ ] Web form for medical case input (patient demographics, symptoms, tests)
- [ ] AI prompt engineering to Claude/GPT-4 for case summary generation
- [ ] 3 quality tiers (Free 1/mo, Pro $10/mo, Enterprise API)
- [ ] Simple dashboard showing write history
- [ ] User authentication (email signup)
- [ ] Stripe subscription payment
- [ ] Email onboarding

### SHOULD-HAVE
- [ ] Export as DOCX
- [ ] Custom templates per specialty (cardiology, neurology, etc)
- [ ] Usage analytics

### WON'T-HAVE
- [ ] Real-time collaboration
- [ ] Advanced formatting

## Tech Stack (Lighter)

```
Backend:        FastAPI + Supabase (Auth + DB combined)
LLM:            OpenAI GPT-4 API ($0.01-0.03 per generation)
Payment:        Stripe
Frontend:       React + TypeScript
Deploy:         Vercel + Supabase (fully serverless, $0-50/mo)
```

## Timeline: 4-6 Weeks

1. **Week 1**: FastAPI + Supabase setup + Stripe
2. **Week 2**: Claude/GPT-4 integration + prompt engineering
3. **Week 3**: React UI
4. **Week 4**: Auth + Stripe payment
5. **Week 5-6**: Launch + iterate

## Expected MRR
- Month 1-2: $0 (bootstrapping)
- Month 3: $200-500 (beta users)
- Month 6: $1K-2K (SEO + content marketing)
- Month 12: $5K-10K (passive content marketing)

---

# Idea #7: AI Code Review SaaS

## MVP Scope

### MUST-HAVE
- [ ] GitHub App creation (request code review via comment)
- [ ] Integration with Claude/GPT-4 API
- [ ] Security vulnerability detection
- [ ] Performance suggestions
- [ ] Best practices feedback
- [ ] Post review as PR comment
- [ ] Freemium model (3 reviews/month free, then $20/mo)
- [ ] Simple usage dashboard

### SHOULD-HAVE
- [ ] Custom rulesets per team
- [ ] Slack notifications
- [ ] Weekly summary email

## Tech Stack

```
Backend:        FastAPI + Supabase
GitHub App:     PyGithub library
LLM:            Claude Sonnet ($0.003-0.015 per review)
Deploy:         Heroku or Railway (free tier available)
Frontend:       Minimal (mostly GitHub + email notifications)
```

## Timeline: 3-4 Weeks

1. **Week 1**: GitHub App setup + FastAPI webhook receiver
2. **Week 2**: Claude code analysis integration + prompt tuning
3. **Week 3**: PR comment posting + dashboard
4. **Week 4**: Deploy + GitHub App review submission

## Expected MRR
- Month 1: $0 (launch)
- Month 2-3: $200-500 (developer communities, Reddit, HN)
- Month 6: $2K-5K (growing user base, word-of-mouth)
- Month 12: $5K-30K (enterprise pilots)

---

## Vector Database Decision Matrix (For Idea #1)

### Chroma (RECOMMENDED FOR MVP)
- **Pros**: Embedded (0 infra), free, your existing knowledge, 35ms latency, Python-first
- **Cons**: Scales only to ~10M vectors (good enough for 100 users × 1000 docs)
- **Cost**: $0 (embedded) or $20/mo (managed beta)
- **When to upgrade**: 50M+ vectors or need 99.9% SLA

### Pinecone (Production Scale)
- **Pros**: Fully managed, scales to 100B+ vectors, 50ms latency, guaranteed uptime
- **Cons**: $840+/month (for 100M vectors)
- **When to switch**: After proving MRR > $5K/month

### Migration Path
```
MVP (Month 1-3):     Chroma embedded in Docker
Scaling (Month 4-6): Still Chroma (1-10M vectors)
Enterprise (Month 12+): Migrate to Pinecone ($840/mo) when CAC < $200 per customer
```

---

## FastAPI Architecture Best Practices (2025)

### Folder Structure
```
app/
├── core/
│   ├── config.py          # Environment variables
│   ├── security.py        # JWT, password hashing
│   └── exceptions.py      # Custom exceptions
├── api/
│   ├── v1/
│   │   ├── __init__.py
│   │   ├── auth.py        # Auth endpoints
│   │   ├── documents.py   # Document routes
│   │   └── health.py      # Health check
├── models/
│   ├── user.py            # SQLAlchemy models
│   ├── document.py
│   └── subscription.py
├── schemas/
│   ├── user.py            # Pydantic request/response
│   └── document.py
├── services/
│   ├── pdf_service.py     # Business logic
│   ├── embedding_service.py
│   └── llm_service.py
├── database/
│   ├── base.py            # SQLAlchemy base
│   ├── session.py         # DB session factory
│   └── crud.py            # CRUD operations
├── middleware/
│   ├── error_handler.py
│   └── cors.py
└── main.py                # FastAPI app entry
```

### Key Patterns
```python
# 1. Dependency Injection
from fastapi import Depends

async def get_current_user(token: str = Depends(oauth2_scheme)) -> User:
    return validate_token(token)

@router.get("/profile")
async def get_profile(current_user: User = Depends(get_current_user)):
    return current_user

# 2. Async/Await Everything
@router.post("/documents/")
async def upload_document(file: UploadFile):
    # Don't block event loop
    task = celery_app.delay(process_pdf, file.filename)
    return {"status": "processing"}

# 3. Type Hints for Validation
from pydantic import BaseModel, EmailStr

class UserCreate(BaseModel):
    email: EmailStr
    password: str
    name: str

@router.post("/signup/")
async def signup(user: UserCreate):  # Auto-validated by Pydantic
    ...
```

---

## Deployment Checklist

- [ ] Environment variables (.env.example committed, .env in .gitignore)
- [ ] Docker build works (`docker build -t app .`)
- [ ] Docker Compose works locally (`docker-compose up`)
- [ ] Database migrations scripted (Alembic)
- [ ] Logging configured (Sentry or similar)
- [ ] Rate limiting enabled (aioredis)
- [ ] CORS configured for frontend domain
- [ ] Health check endpoint working
- [ ] Database backups automated (RDS, Supabase handle this)
- [ ] SSL/TLS certificate (Let's Encrypt via Heroku/AWS)
- [ ] CI/CD pipeline (.github/workflows/deploy.yml)

---

## Next Steps

1. **Validate Idea #1 with 3-5 potential customers** (clinic managers, physicians)
   - Do they have this problem? (PDF medical docs piling up)
   - Would they pay $50-100/month?
   - Demo a quick MVP prototype (even manual)

2. **Start with Idea #2 (AI Writer) if validation shows low interest in #1**
   - Easier to build, lower barrier to market
   - Can validate writer-for-medical-docs faster

3. **Pick ONE and commit** — avoid building all 3 at once

4. **Build skeleton FastAPI project** with:
   - PostgreSQL + SQLAlchemy
   - JWT auth
   - Stripe webhook
   - Celery for async
   - Docker + Compose
   - GitHub Actions CI/CD

---

## Resources to Bookmark

- FastAPI Docs: https://fastapi.tiangolo.com/
- Chroma Docs: https://docs.trychroma.com/
- Supabase Docs: https://supabase.com/docs
- Stripe Python SDK: https://stripe.com/docs/libraries/python
- Clean Architecture in FastAPI: https://github.com/rafsaf/fastapi-clean-architecture
