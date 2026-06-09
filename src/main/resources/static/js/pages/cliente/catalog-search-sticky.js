(() => {
  const MOBILE_MAX_WIDTH = 767.98;
  const HIDE_SCROLL_DELTA = 8;
  const REVEAL_NEAR_TOP_Y = 96;
  const RESULTS_COLLISION_OFFSET = 140;

  function shouldManageStickySearch() {
    return window.matchMedia(`(min-width: ${MOBILE_MAX_WIDTH + 0.02}px)`).matches;
  }

  function bootstrap() {
    const searchPanel = document.getElementById("catalog-search");
    const resultsAnchor =
      document.querySelector(".catalog-results") ||
      document.querySelector(".catalog-sections") ||
      document.querySelector(".empty-state");
    if (!searchPanel) {
      return;
    }

    let lastY = window.scrollY;
    let ticking = false;

    const syncState = () => {
      const currentY = window.scrollY;
      const delta = currentY - lastY;

      if (!shouldManageStickySearch()) {
        searchPanel.classList.remove("catalog-search--hidden");
        lastY = currentY;
        ticking = false;
        return;
      }

      if (currentY <= REVEAL_NEAR_TOP_Y) {
        searchPanel.classList.remove("catalog-search--hidden");
      } else if (
        resultsAnchor &&
        resultsAnchor.getBoundingClientRect().top <= RESULTS_COLLISION_OFFSET &&
        delta >= 0
      ) {
        searchPanel.classList.add("catalog-search--hidden");
      } else if (delta > HIDE_SCROLL_DELTA) {
        searchPanel.classList.add("catalog-search--hidden");
      } else if (delta < -HIDE_SCROLL_DELTA) {
        searchPanel.classList.remove("catalog-search--hidden");
      }

      lastY = currentY;
      ticking = false;
    };

    const requestSync = () => {
      if (ticking) {
        return;
      }
      ticking = true;
      window.requestAnimationFrame(syncState);
    };

    window.addEventListener("scroll", requestSync, { passive: true });
    window.addEventListener("resize", requestSync, { passive: true });
    searchPanel.addEventListener("mouseenter", () => {
      searchPanel.classList.remove("catalog-search--hidden");
    });
    searchPanel.addEventListener("focusin", () => {
      searchPanel.classList.remove("catalog-search--hidden");
    });

    syncState();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", bootstrap, { once: true });
  } else {
    bootstrap();
  }
})();
