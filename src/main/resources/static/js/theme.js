// Global theme manager for consistent dark/light mode across pages
// Applies `theme-dark` on <html> and persists choice in localStorage
(function(){
  const KEY = 'illusionix-theme';
  const DARK = 'theme-dark';

  function apply(theme){
    const root = document.documentElement;
    if(theme === 'dark') root.classList.add(DARK); else root.classList.remove(DARK);
  }

  function get(){
    try { return localStorage.getItem(KEY); } catch(_) { return null; }
  }

  function set(theme){
    try { localStorage.setItem(KEY, theme); } catch(_) {}
  }

  function current(){
    return document.documentElement.classList.contains(DARK) ? 'dark' : 'light';
  }

  function syncButtons(theme){
    // Toggle sun/moon icons and aria state
    document.querySelectorAll('.theme-toggle [data-icon="sun"]').forEach(el => {
      el.style.display = theme === 'dark' ? 'inline-block' : 'none';
    });
    document.querySelectorAll('.theme-toggle [data-icon="moon"]').forEach(el => {
      el.style.display = theme === 'dark' ? 'none' : 'inline-block';
    });
    document.querySelectorAll('.theme-toggle').forEach(btn => {
      btn.setAttribute('aria-pressed', theme === 'dark' ? 'true' : 'false');
      btn.setAttribute('title', theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
    });
  }

  function toggle(){
    const next = current() === 'dark' ? 'light' : 'dark';
    apply(next); set(next); syncButtons(next);
  }

  function init(){
    // Initialize from saved theme or default to light
    const saved = get() || 'light';
    apply(saved);
    // Bind buttons
    document.querySelectorAll('.theme-toggle').forEach(btn => {
      btn.addEventListener('click', toggle);
    });
    syncButtons(current());
  }

  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
