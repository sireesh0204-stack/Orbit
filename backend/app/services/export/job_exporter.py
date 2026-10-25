import io
import csv
from datetime import datetime
from typing import List
from app.models.job import JobPosting, RankedJob

def export_jobs_to_csv(jobs: List[JobPosting]) -> Tuple_Csv:
    """Exports list of jobs as CSV formatted text bytes."""
    output = io.StringIO()
    writer = csv.writer(output)

    # Headers
    writer.writerow([
        "Job ID",
        "Title",
        "Company",
        "Location",
        "Primary Platform",
        "Apply URL",
        "Match Percentage",
        "Posted Date",
        "Expiry Date",
        "Is Favorite"
    ])

    for j in jobs:
        match_pct = getattr(j, "match_percentage", "")
        writer.writerow([
            j.job_id,
            j.title,
            j.company,
            j.location,
            j.primary_platform,
            j.apply_url,
            match_pct,
            j.posted_date,
            j.expiry_date,
            "YES" if j.is_favorite else "NO"
        ])

    csv_bytes = output.getvalue().encode("utf-8")
    now_str = datetime.now().strftime("%Y-%m-%d_%H%M")
    filename = f"jobs_{now_str}.csv"
    return csv_bytes, filename

def export_jobs_to_html(jobs: List[JobPosting]) -> Tuple_Csv:
    """Exports list of jobs as standalone styled HTML."""
    now_str = datetime.now().strftime("%Y-%m-%d_%H%M")
    filename = f"jobs_{now_str}.html"

    rows = []
    for j in jobs:
        match_val = getattr(j, "match_percentage", "N/A")
        rows.append(f"""
        <tr>
            <td style="font-weight:bold; color:#00F0FF;">{j.title}</td>
            <td>{j.company}</td>
            <td>{j.location}</td>
            <td><span style="color:#00E676; font-weight:bold;">{match_val}%</span></td>
            <td>{j.posted_date}</td>
            <td>{j.expiry_date}</td>
            <td><a href="{j.apply_url}" target="_blank" style="color:#E5B869;">Apply</a></td>
        </tr>
        """)

    html_content = f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>JobPilot Export - {now_str}</title>
    <style>
        body {{ background-color: #030508; color: #F1F5F9; font-family: -apple-system, BlinkMacSystemFont, sans-serif; padding: 24px; }}
        h1 {{ color: #00F0FF; margin-bottom: 4px; }}
        table {{ width: 100%; border-collapse: collapse; margin-top: 16px; }}
        th, td {{ border: 1px solid rgba(255,255,255,0.12); padding: 10px 14px; text-align: left; }}
        th {{ background-color: #0B0F17; color: #94A3B8; font-size: 12px; text-transform: uppercase; }}
        tr:nth-child(even) {{ background-color: #0B0F17; }}
    </style>
</head>
<body>
    <h1>JobPilot Verified Job Archive</h1>
    <p style="color: #64748B;">Generated {now_str} • Dual-User Private Portfolio</p>
    <table>
        <thead>
            <tr>
                <th>Title</th>
                <th>Company</th>
                <th>Location</th>
                <th>Match</th>
                <th>Posted</th>
                <th>Expiry</th>
                <th>Link</th>
            </tr>
        </thead>
        <tbody>
            {"".join(rows)}
        </tbody>
    </table>
</body>
</html>"""

    return html_content.encode("utf-8"), filename

# Type alias
Tuple_Csv = tuple[bytes, str]

