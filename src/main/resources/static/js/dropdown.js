// Better Dropdown Behavior
// This script adds a delay before closing dropdowns to make them easier to use

document.addEventListener('DOMContentLoaded', function() {
    let dropdownTimeout = null;

    // Handle navigation dropdowns
    const navDropdowns = document.querySelectorAll('.nav-item.dropdown');
    navDropdowns.forEach(dropdown => {
        const menu = dropdown.querySelector('.dropdown-menu');
        if (!menu) return;

        dropdown.addEventListener('mouseenter', function() {
            clearTimeout(dropdownTimeout);
            menu.style.display = 'block';
        });

        dropdown.addEventListener('mouseleave', function() {
            dropdownTimeout = setTimeout(() => {
                menu.style.display = 'none';
            }, 300); // 300ms delay
        });

        menu.addEventListener('mouseenter', function() {
            clearTimeout(dropdownTimeout);
        });

        menu.addEventListener('mouseleave', function() {
            dropdownTimeout = setTimeout(() => {
                menu.style.display = 'none';
            }, 300);
        });
    });

    // Handle profile dropdown
    const profileDropdown = document.querySelector('.nav-profile-dropdown');
    if (profileDropdown) {
        const profileMenu = profileDropdown.querySelector('.nav-profile-menu');
        if (profileMenu) {
            profileDropdown.addEventListener('mouseenter', function() {
                clearTimeout(dropdownTimeout);
                profileMenu.style.display = 'block';
            });

            profileDropdown.addEventListener('mouseleave', function() {
                dropdownTimeout = setTimeout(() => {
                    profileMenu.style.display = 'none';
                }, 300);
            });

            profileMenu.addEventListener('mouseenter', function() {
                clearTimeout(dropdownTimeout);
            });

            profileMenu.addEventListener('mouseleave', function() {
                dropdownTimeout = setTimeout(() => {
                    profileMenu.style.display = 'none';
                }, 300);
            });
        }
    }

    // Click outside to close
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.dropdown') && !e.target.closest('.nav-profile-dropdown')) {
            document.querySelectorAll('.dropdown-menu, .nav-profile-menu').forEach(menu => {
                menu.style.display = 'none';
            });
        }
    });
});