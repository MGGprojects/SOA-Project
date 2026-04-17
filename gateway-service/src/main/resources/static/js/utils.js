export function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

export function formatDate(value) {
    if (!value) {
        return "Unknown date";
    }

    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    }).format(new Date(value));
}

export function formatTime(value) {
    if (!value) {
        return "--:--";
    }

    return new Intl.DateTimeFormat("en-GB", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
        timeZone: "UTC"
    }).format(new Date(value)) + " UTC";
}

export function toUtcIsoString(value) {
    return new Date(value).toISOString();
}

export function overlaps(startA, endA, startB, endB) {
    return new Date(startA).getTime() < new Date(endB).getTime()
        && new Date(endA).getTime() > new Date(startB).getTime();
}

export function buildGoogleMapsEmbedUrl(venue, city) {
    const query = [venue, city].filter(Boolean).join(", ");
    return `https://maps.google.com/maps?q=${encodeURIComponent(query)}&z=15&output=embed`;
}
