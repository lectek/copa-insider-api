if (!window.__RMF_ADMIN_SHORTCUTS_BOOTSTRAPPED__) {
  window.__RMF_ADMIN_SHORTCUTS_BOOTSTRAPPED__ = true;

  const ready = (callback) => {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", callback, { once: true });
      return;
    }
    callback();
  };

  ready(() => {
    const quickSaleLink = document.querySelector(
      "[data-admin-shortcut='venda-rapida']"
    );
    const quickSaleUrl = quickSaleLink?.getAttribute("href") || "/admin/vendas/rapida";
    if (!quickSaleUrl) return;

    const quickSalePath = (() => {
      try {
        return new URL(quickSaleUrl, window.location.origin).pathname;
      } catch {
        return quickSaleUrl;
      }
    })();

    document.addEventListener("keydown", (event) => {
      const isF3 =
        event.key === "F3" ||
        event.code === "F3" ||
        event.keyCode === 114;
      if (!isF3 || event.defaultPrevented || event.repeat) return;
      if (event.ctrlKey || event.altKey || event.metaKey) return;

      event.preventDefault();

      if (window.location.pathname === quickSalePath) {
        const searchInput = document.getElementById("pdv-term");
        if (searchInput) {
          searchInput.focus();
          if (typeof searchInput.select === "function") {
            searchInput.select();
          }
        }
        return;
      }

      window.location.assign(quickSaleUrl);
    });
  });
}
