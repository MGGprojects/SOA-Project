import { buildGoogleMapsEmbedUrl, escapeHtml, formatDate, formatTime } from "./utils.js";

export const elements = {
    eventList: document.getElementById("eventList"),
    statusBadge: document.getElementById("statusBadge"),
    refreshButton: document.getElementById("refreshButton"),
    emptyState: document.getElementById("emptyState"),
    eventDetail: document.getElementById("eventDetail"),
    exportStatus: document.getElementById("exportStatus"),
    detailBusiness: document.getElementById("detailBusiness"),
    detailTitle: document.getElementById("detailTitle"),
    detailDate: document.getElementById("detailDate"),
    detailTime: document.getElementById("detailTime"),
    detailVenue: document.getElementById("detailVenue"),
    detailId: document.getElementById("detailId"),
    detailDescription: document.getElementById("detailDescription"),
    googleLink: document.getElementById("googleLink"),
    outlookLink: document.getElementById("outlookLink"),
    icsLink: document.getElementById("icsLink"),
    registerForm: document.getElementById("registerForm"),
    loginForm: document.getElementById("loginForm"),
    logoutButton: document.getElementById("logoutButton"),
    authStatus: document.getElementById("authStatus"),
    sessionBadge: document.getElementById("sessionBadge"),
    sessionUserId: document.getElementById("sessionUserId"),
    sessionEmail: document.getElementById("sessionEmail"),
    sessionRole: document.getElementById("sessionRole"),
    businessForm: document.getElementById("businessForm"),
    eventForm: document.getElementById("eventForm"),
    profileForm: document.getElementById("profileForm"),
    profileStatus: document.getElementById("profileStatus"),
    profileSummary: document.getElementById("profileSummary"),
    profileUserId: document.getElementById("profileUserId"),
    profileName: document.getElementById("profileName"),
    protectedStatus: document.getElementById("protectedStatus"),
    businessContactEmail: document.getElementById("businessContactEmail"),
    eventBusinessId: document.getElementById("eventBusinessId"),
    competitionStatus: document.getElementById("competitionStatus"),
    toastContainer: document.getElementById("toastContainer"),
    detailMap: document.getElementById("detailMap"),
    filterForm: document.getElementById("filterForm"),
    filterKeyword: document.getElementById("filterKeyword"),
    filterCity: document.getElementById("filterCity"),
    filterDate: document.getElementById("filterDate"),
    filterBusinessId: document.getElementById("filterBusinessId"),
    clearFiltersButton: document.getElementById("clearFiltersButton"),
    favoriteButton: document.getElementById("favoriteButton"),
    favoriteBadge: document.getElementById("favoriteBadge"),
    favoritesCount: document.getElementById("favoritesCount"),
    favoritesList: document.getElementById("favoritesList"),
    registerTabButton: document.getElementById("registerTabButton"),
    loginTabButton: document.getElementById("loginTabButton"),
    registerPane: document.getElementById("registerPane"),
    loginPane: document.getElementById("loginPane"),
    customerTabButton: document.getElementById("customerTabButton"),
    businessTabButton: document.getElementById("businessTabButton"),
    customerPane: document.getElementById("customerPane"),
    businessPane: document.getElementById("businessPane")
};

export function showToast(title, message, tone = "success") {
    const toast = document.createElement("article");
    toast.className = `toast ${tone === "error" ? "toast-error" : "toast-success"}`;
    toast.innerHTML = `
        <strong>${escapeHtml(title)}</strong>
        <span>${escapeHtml(message)}</span>
    `;
    elements.toastContainer.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

export function updateSessionUi(session) {
    if (!session?.token) {
        elements.sessionBadge.textContent = "Guest mode";
        elements.sessionBadge.className = "status-badge neutral-badge";
        elements.sessionUserId.textContent = "-";
        elements.sessionEmail.textContent = "-";
        elements.sessionRole.textContent = "-";
        elements.protectedStatus.textContent = "Login as BUSINESS to unlock protected actions.";
        elements.favoriteBadge.textContent = "Login to use favorites";
        return;
    }

    elements.sessionBadge.textContent = `${session.role} session`;
    elements.sessionBadge.className = `status-badge ${session.role === "BUSINESS" ? "success-badge" : "warning-badge"}`;
    elements.sessionUserId.textContent = session.userId ?? "-";
    elements.sessionEmail.textContent = session.email ?? "-";
    elements.sessionRole.textContent = session.role ?? "-";
    elements.protectedStatus.textContent = session.role === "BUSINESS"
        ? "You can now create businesses and events through the gateway."
        : "This session is valid. Create a user profile to save favorites.";
}

export function updateProfileUi(profile) {
    if (!profile) {
        elements.profileSummary.classList.add("hidden");
        elements.profileStatus.textContent = "Login to create a customer profile and use favorites.";
        elements.favoriteBadge.textContent = "Profile needed";
        return;
    }

    elements.profileSummary.classList.remove("hidden");
    elements.profileUserId.textContent = profile.userId;
    elements.profileName.textContent = `${profile.firstName} ${profile.lastName}`;
    elements.profileStatus.textContent = "Profile ready. You can now save favorite events.";
    elements.favoriteBadge.textContent = "Favorites enabled";
}

export function showFlowTab(tabName) {
    const showCustomer = tabName === "customer";
    elements.customerPane.classList.toggle("hidden", !showCustomer);
    elements.businessPane.classList.toggle("hidden", showCustomer);
    elements.customerPane.hidden = !showCustomer;
    elements.businessPane.hidden = showCustomer;
    elements.customerPane.setAttribute("aria-hidden", String(!showCustomer));
    elements.businessPane.setAttribute("aria-hidden", String(showCustomer));
    elements.customerTabButton.classList.toggle("active-tab", showCustomer);
    elements.businessTabButton.classList.toggle("active-tab", !showCustomer);
    elements.customerTabButton.setAttribute("aria-pressed", String(showCustomer));
    elements.businessTabButton.setAttribute("aria-pressed", String(!showCustomer));
}

export function showAuthTab(tabName) {
    const showRegister = tabName === "register";
    elements.registerPane.classList.toggle("hidden", !showRegister);
    elements.loginPane.classList.toggle("hidden", showRegister);
    elements.registerPane.hidden = !showRegister;
    elements.loginPane.hidden = showRegister;
    elements.registerPane.setAttribute("aria-hidden", String(!showRegister));
    elements.loginPane.setAttribute("aria-hidden", String(showRegister));
    elements.registerTabButton.classList.toggle("active-tab", showRegister);
    elements.loginTabButton.classList.toggle("active-tab", !showRegister);
    elements.registerTabButton.setAttribute("aria-pressed", String(showRegister));
    elements.loginTabButton.setAttribute("aria-pressed", String(!showRegister));
}

export function renderEventList(events, selectedEventId, favoriteIds, onSelect) {
    if (!events.length) {
        elements.eventList.innerHTML = createInfoCard("No events match the current filters.");
        return;
    }

    elements.eventList.innerHTML = "";

    events.forEach((event) => {
        const card = document.createElement("button");
        const isFavorite = favoriteIds.has(event.eventId);
        card.type = "button";
        card.className = `event-card${event.eventId === selectedEventId ? " selected" : ""}${isFavorite ? " favorite-card" : ""}`;
        card.innerHTML = `
            <div class="event-card-top">
                <div>
                    <h3>${escapeHtml(event.title)}</h3>
                    <p>${escapeHtml(event.description || "No description available.")}</p>
                    ${isFavorite ? '<span class="favorite-chip">Saved favorite</span>' : ""}
                </div>
                <span class="pill">${formatDate(event.startTime)}</span>
            </div>
            <div class="event-card-bottom">
                <div class="event-meta">
                    <span><strong>Venue:</strong> ${escapeHtml(event.venue || "TBD")}</span>
                    <span><strong>Business:</strong> ${escapeHtml(event.businessId || "Unknown")}</span>
                </div>
                <span>View details</span>
            </div>
        `;
        card.addEventListener("click", () => onSelect(event.eventId));
        elements.eventList.appendChild(card);
    });
}

export function renderDetail(event, isFavorite) {
    showDetailShell();
    elements.detailBusiness.textContent = event.business?.name
        ? `Hosted by ${event.business.name}`
        : `Business ID ${event.businessId || "not available"}`;
    elements.detailTitle.textContent = event.title || "Untitled event";
    elements.detailDate.textContent = formatDate(event.startTime);
    elements.detailTime.textContent = `${formatTime(event.startTime)} - ${formatTime(event.endTime)}`;
    elements.detailVenue.textContent = event.venue || "TBD";
    elements.detailId.textContent = event.eventId || "-";
    elements.detailDescription.textContent = event.description || "No description available.";
    elements.detailMap.src = buildGoogleMapsEmbedUrl(event.venue, event.city);
    elements.favoriteButton.textContent = isFavorite ? "Remove from favorites" : "Add to favorites";
}

export function renderFavoriteEvents(events, onSelect) {
    elements.favoritesCount.textContent = `${events.length} saved`;

    if (!events.length) {
        elements.favoritesList.innerHTML = createInfoCard("No favorite events yet.");
        return;
    }

    elements.favoritesList.innerHTML = "";
    events.forEach((event) => {
        const row = document.createElement("article");
        row.className = "favorite-item";
        row.innerHTML = `
            <div>
                <h4>${escapeHtml(event.title)}</h4>
                <p>${formatDate(event.startTime)} at ${escapeHtml(event.venue || "TBD")}</p>
            </div>
            <button class="button button-secondary" type="button">Open</button>
        `;
        row.querySelector("button").addEventListener("click", () => onSelect(event.eventId));
        elements.favoritesList.appendChild(row);
    });
}

export function clearDetail() {
    elements.emptyState.classList.remove("hidden");
    elements.eventDetail.classList.add("hidden");
    clearExportLinks();
    elements.exportStatus.textContent = "Ready";
    elements.detailMap.src = "";
    elements.favoriteButton.textContent = "Add to favorites";
}

export function showDetailShell() {
    elements.emptyState.classList.add("hidden");
    elements.eventDetail.classList.remove("hidden");
}

export function setExportLinks(links) {
    elements.googleLink.href = links.googleLink;
    elements.outlookLink.href = links.outlookLink;
    elements.icsLink.href = links.calendarLink;
    elements.googleLink.classList.remove("disabled-link");
    elements.outlookLink.classList.remove("disabled-link");
    elements.icsLink.classList.remove("disabled-link");
    elements.exportStatus.textContent = "Export links ready";
}

export function clearExportLinks() {
    [elements.googleLink, elements.outlookLink, elements.icsLink].forEach((anchor) => {
        anchor.href = "#";
        anchor.classList.add("disabled-link");
    });
}

export function createInfoCard(message, isError = false) {
    return `
        <article class="detail-card ${isError ? "error-card" : ""}">
            <h3>${isError ? "Something went wrong" : "Heads up"}</h3>
            <p>${escapeHtml(message)}</p>
        </article>
    `;
}
