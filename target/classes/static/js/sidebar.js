// Sidebar toggle functionality
document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    const toggleBtn = document.getElementById('sidebarToggle');
    const closeBtn = document.getElementById('sidebarClose');
    const menuItems = document.querySelectorAll('.menu-item');

    // Check if elements exist
    if (!sidebar || !toggleBtn) {
        console.warn('Sidebar elements not found');
        return;
    }

    // Load saved state from localStorage
    const savedState = localStorage.getItem('sidebarCollapsed');
    if (savedState === 'true' && window.innerWidth > 768) {
        sidebar.classList.add('collapsed');
        document.querySelector('.main-content')?.classList.add('sidebar-collapsed');
    }

    // Toggle sidebar
    function toggleSidebar() {
        const isCollapsed = sidebar.classList.toggle('collapsed');
        document.querySelector('.main-content')?.classList.toggle('sidebar-collapsed');
        
        // Save state only for desktop
        if (window.innerWidth > 768) {
            localStorage.setItem('sidebarCollapsed', isCollapsed);
        }
    }

    // Open sidebar (mobile)
    function openSidebar() {
        sidebar.classList.add('active');
        if (overlay) {
            overlay.classList.add('active');
        }
    }

    // Close sidebar (mobile)
    function closeSidebar() {
        sidebar.classList.remove('active');
        if (overlay) {
            overlay.classList.remove('active');
        }
    }

    // Event listeners
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            if (window.innerWidth <= 768) {
                // Mobile: open sidebar with overlay
                if (sidebar.classList.contains('active')) {
                    closeSidebar();
                } else {
                    openSidebar();
                }
            } else {
                // Desktop: collapse sidebar
                toggleSidebar();
            }
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', closeSidebar);
    }

    if (overlay) {
        overlay.addEventListener('click', closeSidebar);
    }

    // Close sidebar when clicking menu item on mobile
    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            if (window.innerWidth <= 768) {
                closeSidebar();
            }
        });
    });

    // Handle window resize
    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            if (window.innerWidth > 768) {
                // Desktop: remove mobile classes
                sidebar.classList.remove('active');
                if (overlay) {
                    overlay.classList.remove('active');
                }
            } else {
                // Mobile: remove collapsed class
                sidebar.classList.remove('collapsed');
                document.querySelector('.main-content')?.classList.remove('sidebar-collapsed');
            }
        }, 250);
    });
});
