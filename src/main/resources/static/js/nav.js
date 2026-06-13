/* nav.js — navegação pública Copa Insider */
(function () {
    'use strict';

    var nav    = document.querySelector('.nav');
    var toggle = nav && nav.querySelector('.nav__toggle');

    if (!nav || !toggle) return;

    function openMenu()  { nav.classList.add('nav--open');    document.body.style.overflow = 'hidden'; }
    function closeMenu() { nav.classList.remove('nav--open'); document.body.style.overflow = ''; }
    function isOpen()    { return nav.classList.contains('nav--open'); }

    toggle.addEventListener('click', function (e) {
        e.stopPropagation();
        isOpen() ? closeMenu() : openMenu();
    });

    /* fechar ao clicar fora */
    document.addEventListener('click', function (e) {
        if (isOpen() && !nav.contains(e.target)) closeMenu();
    });

    /* fechar com Escape */
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && isOpen()) closeMenu();
    });

    /* fechar ao redimensionar para desktop */
    window.addEventListener('resize', function () {
        if (window.innerWidth > 768 && isOpen()) closeMenu();
    });

    /* marcar link activo via meta tag injectada pelo fragment public-head */
    var meta   = document.querySelector('meta[name="pagina-actual"]');
    var pagina = meta && meta.getAttribute('content');
    if (pagina) {
        var links = nav.querySelectorAll('.nav__link[data-pagina]');
        links.forEach(function (link) {
            if (link.dataset.pagina === pagina) link.classList.add('nav__link--active');
        });
    }
}());
