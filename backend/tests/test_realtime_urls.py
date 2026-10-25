import urllib.parse
from app.services.scrapers.url_builder import RealTimeJobUrlBuilder

def test_build_linkedin_realtime_url_5_minutes():
    url = RealTimeJobUrlBuilder.build_linkedin_realtime_url(
        keywords="Staff Backend Engineer",
        location="Hyderabad",
        seconds_ago=300,
        work_type="all"
    )
    parsed = urllib.parse.urlparse(url)
    params = urllib.parse.parse_qs(parsed.query)

    assert parsed.netloc == "www.linkedin.com"
    assert params["keywords"] == ["Staff Backend Engineer"]
    assert params["location"] == ["Hyderabad"]
    assert params["sortBy"] == ["DD"]  # Strict Date Descending
    assert params["f_TPR"] == ["r300"]  # 5 minutes in seconds
    assert params["f_WT"] == ["1,2,3"]  # Office, Remote, Hybrid in Hyderabad

def test_build_linkedin_realtime_url_remote_only():
    url = RealTimeJobUrlBuilder.build_linkedin_realtime_url(
        keywords="Lead Systems Architect",
        location="India",
        seconds_ago=600,
        work_type="remote_only"
    )
    parsed = urllib.parse.urlparse(url)
    params = urllib.parse.parse_qs(parsed.query)

    assert params["sortBy"] == ["DD"]
    assert params["f_TPR"] == ["r600"]  # 10 minutes
    assert params["f_WT"] == ["2"]  # Strictly Remote only

def test_build_naukri_realtime_url():
    url = RealTimeJobUrlBuilder.build_naukri_realtime_url(
        keywords="Android Engineer",
        location="Hyderabad",
        remote_only=False
    )
    assert "naukri.com/android-engineer-jobs-in-hyderabad" in url
    parsed = urllib.parse.urlparse(url)
    params = urllib.parse.parse_qs(parsed.query)
    assert params["sort"] == ["date"]
    assert params["freshness"] == ["1"]

def test_build_indeed_realtime_url():
    url = RealTimeJobUrlBuilder.build_indeed_realtime_url(
        keywords="Python Engineer",
        location="Hyderabad",
        remote_only=False
    )
    parsed = urllib.parse.urlparse(url)
    params = urllib.parse.parse_qs(parsed.query)
    assert params["sort"] == ["date"]
    assert params["q"] == ["Python Engineer"]

def test_generate_candidate_feed_urls():
    targets = RealTimeJobUrlBuilder.generate_candidate_feed_urls(
        target_roles=["Staff Backend Engineer"],
        seconds_ago=300
    )
    assert len(targets) == 4
    # Ensure Hyderabad office/hybrid/remote and Pan-India remote are generated
    platforms = [t["platform"] for t in targets]
    assert "LinkedIn" in platforms
    assert "Naukri" in platforms

    # Confirm 5-min LinkedIn filter
    linkedin_targets = [t for t in targets if t["platform"] == "LinkedIn"]
    for lt in linkedin_targets:
        assert "f_TPR=r300" in lt["url"]
        assert "sortBy=DD" in lt["url"]

