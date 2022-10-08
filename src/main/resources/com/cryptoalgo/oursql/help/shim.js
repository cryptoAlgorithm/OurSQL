// Shim to make links non-clickable and images non-draggable
document.querySelectorAll('a').forEach(l => {
    if (l.getAttribute('href').startsWith('#')) return;
    l.style.cursor = 'default';
    l.onclick = e => e.preventDefault();
});
document.querySelectorAll('img').forEach(i => {
    i.ondragstart = () => false;
});