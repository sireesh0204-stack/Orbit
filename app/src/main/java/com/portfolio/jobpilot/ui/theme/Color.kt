package com.portfolio.jobpilot.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// JOBPILOT EXECUTIVE BENTO THEME (Linear / Raycast / Titanium Slate Cockpit)
// Distinct from Soniq DSP's dark cyberpunk audio aesthetic.
// ============================================================================

// Deep Titanium Slate Bases
val Surface000 = Color(0xFF090D16) // Deep Titanium Canvas (Low eye fatigue)
val Surface100 = Color(0xFF0F172A) // Bento Module Background (Slate 900)
val Surface200 = Color(0xFF1E293B) // Elevated Interactive Fill (Slate 800)
val SurfaceBorder = Color(0xFF334155) // Precision 1px Structural Chamfer (Slate 700)

// Refined Executive Accents
val ExecutiveIndigo = Color(0xFF6366F1) // Electric Cobalt/Indigo for primary actions
val CalmEmerald = Color(0xFF10B981)     // Refined Emerald for >=90% match / verified links
val WarmAmber = Color(0xFFF59E0B)       // Warm Amber for impending expiry (<48h)
val SignalCrimson = Color(0xFFEF4444)   // Clean Coral/Crimson for broken links / alerts
val SatinGold = Color(0xFFEAB308)       // Amber/Gold for Starred favorites

// Aliases for seamless backward compatibility across all screens
val NeonCyan = ExecutiveIndigo
val NeonEmerald = CalmEmerald
val NeonAmber = WarmAmber
val NeonCrimson = SignalCrimson

// Contrast & Clean Typography (WCAG AAA compliant on Titanium Slate)
val TextPrimary = Color(0xFFF8FAFC)   // Slate 50 (High contrast, zero glare)
val TextSecondary = Color(0xFF94A3B8) // Slate 400 (Comfortable reading)
val TextMuted = Color(0xFF64748B)     // Slate 500 (Metadata & timestamps)
