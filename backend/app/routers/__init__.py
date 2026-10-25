from .health import router as health_router
from .profile import router as profile_router
from .jobs import router as jobs_router

__all__ = ["health_router", "profile_router", "jobs_router"]

