import pytest
from app.config import settings
from app.services.scrapers.apify_client import ApifyTokenPool, fetch_jobs_from_scrapers, get_mock_scraped_jobs

def test_apify_tokens_loaded_from_env():
    """Verify that settings parses the 4 Apify tokens from .env"""
    assert len(settings.apify_tokens) >= 4
    for token in settings.apify_tokens:
        assert token.startswith("apify_api_")

def test_apify_token_pool_round_robin():
    """Verify round-robin cycling through tokens in order"""
    sample_tokens = ["token_1", "token_2", "token_3", "token_4"]
    pool = ApifyTokenPool(tokens=sample_tokens)
    
    assert pool.total_tokens == 4
    assert len(pool.active_tokens) == 4
    
    # First rotation
    assert pool.get_next_token() == "token_1"
    assert pool.get_next_token() == "token_2"
    assert pool.get_next_token() == "token_3"
    assert pool.get_next_token() == "token_4"
    
    # Second rotation (wraps around seamlessly)
    assert pool.get_next_token() == "token_1"
    assert pool.get_next_token() == "token_2"

def test_apify_token_pool_exhaustion_and_skipping():
    """Verify exhausted tokens are skipped dynamically"""
    sample_tokens = ["token_alpha", "token_beta", "token_gamma"]
    pool = ApifyTokenPool(tokens=sample_tokens)
    
    # Mark beta as exhausted (e.g. hit 429/402 rate limit)
    pool.mark_exhausted("token_beta")
    
    assert pool.total_tokens == 3
    assert pool.active_tokens == ["token_alpha", "token_gamma"]
    
    # Only alpha and gamma should be served
    assert pool.get_next_token() == "token_alpha"
    assert pool.get_next_token() == "token_gamma"
    assert pool.get_next_token() == "token_alpha"

def test_apify_token_pool_auto_recovery_when_all_exhausted():
    """Verify that when all tokens are exhausted, the pool resets gracefully"""
    sample_tokens = ["tok_a", "tok_b"]
    pool = ApifyTokenPool(tokens=sample_tokens)
    
    pool.mark_exhausted("tok_a")
    pool.mark_exhausted("tok_b")
    
    # All exhausted -> Should reset and still provide a token instead of crashing
    next_tok = pool.get_next_token()
    assert next_tok in sample_tokens

def test_empty_token_pool_returns_none():
    pool = ApifyTokenPool(tokens=[])
    assert pool.total_tokens == 0
    assert pool.get_next_token() is None

@pytest.mark.anyio
async def test_fetch_jobs_fallback_resilience():
    """Verify fetch_jobs_from_scrapers returns high-fidelity job postings even offline"""
    jobs = await fetch_jobs_from_scrapers(timeframe="24h")
    assert len(jobs) > 0
    assert any(j.company == "Stripe" for j in jobs)
    assert any(j.company == "Uber" for j in jobs)

