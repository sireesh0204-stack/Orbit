from typing import List
import urllib.parse

class RealTimeJobUrlBuilder:
    """
    Constructs high-precision real-time search URLs for job boards,
    leveraging undocumented / hidden URL query parameter manipulation.
    """

    @staticmethod
    def build_linkedin_realtime_url(
        keywords: str,
        location: str = "Hyderabad",
        seconds_ago: int = 300,  # 300 seconds = 5 minutes
        work_type: str = "all"  # "all" (on-site, hybrid, remote) or "remote_only"
    ) -> str:
        """
        LinkedIn URL Manipulation Trick:
        - Normally, LinkedIn UI only offers 'Past 24 hours' (f_TPR=r86400).
        - However, the backend accepts ANY arbitrary second count prefixed with 'r':
          f_TPR=r300 -> Jobs posted within the last 300 seconds (5 MINUTES)!
          f_TPR=r900 -> Jobs posted within the last 15 minutes!
        - sortBy=DD: Forces strict Chronological Date Descending (newest first),
          bypassing LinkedIn's algorithmic 'Top Matches' (sortBy=R).
        - f_WT: 1=On-site, 2=Remote, 3=Hybrid.
        """
        params = {
            "keywords": keywords,
            "location": location,
            "sortBy": "DD",  # Date Descending (Newest first)
            "f_TPR": f"r{seconds_ago}",  # Sub-hour second-level time window
        }

        if work_type == "remote_only":
            params["f_WT"] = "2"  # Remote only
        elif work_type == "all":
            params["f_WT"] = "1,2,3"  # On-site, Remote, Hybrid (for Hyderabad)

        query_string = urllib.parse.urlencode(params)
        return f"https://www.linkedin.com/jobs/search/?{query_string}"

    @staticmethod
    def build_naukri_realtime_url(
        keywords: str,
        location: str = "Hyderabad",
        remote_only: bool = False
    ) -> str:
        """
        Naukri URL Manipulation:
        - sort=date: Sorts by posting timestamp descending.
        - freshtype=freshness&freshness=1: Limits to latest 24h/active batch.
        """
        encoded_keywords = keywords.replace(" ", "-").lower()
        encoded_location = location.lower()
        
        base_path = f"https://www.naukri.com/{encoded_keywords}-jobs-in-{encoded_location}"
        params = {
            "sort": "date",
            "freshtype": "freshness",
            "freshness": "1"
        }
        if remote_only:
            params["wfhType"] = "0"  # Work from home filter in Naukri

        query_string = urllib.parse.urlencode(params)
        return f"{base_path}?{query_string}"

    @staticmethod
    def build_indeed_realtime_url(
        keywords: str,
        location: str = "Hyderabad",
        remote_only: bool = False
    ) -> str:
        """
        Indeed URL Manipulation:
        - sort=date: Bypasses relevance ordering, sorting by minute posted.
        - fromage=1: Limits search window to most recent batch.
        """
        params = {
            "q": keywords,
            "l": "Remote" if remote_only else location,
            "sort": "date",
            "fromage": "1"
        }
        query_string = urllib.parse.urlencode(params)
        return f"https://in.indeed.com/jobs?{query_string}"

    @classmethod
    def generate_candidate_feed_urls(
        cls,
        target_roles: List[str],
        seconds_ago: int = 300
    ) -> List[dict]:
        """
        Generates live real-time search targets applying our location policy:
        1. Hyderabad: Office, Hybrid, or Remote (f_WT=1,2,3).
        2. Pan-India: Strictly Remote only (f_WT=2).
        """
        targets = []
        for role in target_roles:
            # 1. Hyderabad targets (All work modes)
            targets.append({
                "platform": "LinkedIn",
                "role": role,
                "location": "Hyderabad",
                "work_mode": "Office/Hybrid/Remote",
                "seconds_ago": seconds_ago,
                "url": cls.build_linkedin_realtime_url(
                    keywords=role,
                    location="Hyderabad",
                    seconds_ago=seconds_ago,
                    work_type="all"
                )
            })
            # 2. Pan-India Remote targets
            targets.append({
                "platform": "LinkedIn",
                "role": role,
                "location": "India",
                "work_mode": "Remote Only",
                "seconds_ago": seconds_ago,
                "url": cls.build_linkedin_realtime_url(
                    keywords=role,
                    location="India",
                    seconds_ago=seconds_ago,
                    work_type="remote_only"
                )
            })
            # 3. Naukri real-time targets
            targets.append({
                "platform": "Naukri",
                "role": role,
                "location": "Hyderabad",
                "work_mode": "Office/Hybrid/Remote",
                "url": cls.build_naukri_realtime_url(keywords=role, location="Hyderabad", remote_only=False)
            })
            targets.append({
                "platform": "Naukri",
                "role": role,
                "location": "India",
                "work_mode": "Remote Only",
                "url": cls.build_naukri_realtime_url(keywords=role, location="India", remote_only=True)
            })
        return targets

