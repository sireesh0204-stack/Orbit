from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routers import health_router, profile_router, jobs_router
from app.routers.tailor import router as tailor_router
from app.routers.export import router as export_router
from app.routers.interview import router as interview_router

app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="High-precision private backend for JobPilot.",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_router)
app.include_router(profile_router)
app.include_router(jobs_router)
app.include_router(tailor_router)
app.include_router(export_router)
app.include_router(interview_router)

@app.get("/")
async def root():
    return {
        "message": "JobPilot Backend Operational",
        "docs_url": "/docs",
        "health_url": "/health"
    }

