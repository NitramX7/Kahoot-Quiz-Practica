// Show success/error popup when changes are saved
window.addEventListener('DOMContentLoaded', function () {
    const successAlert = document.querySelector('.alert-success');
    if (successAlert) {
        alert('✓ Cambios guardados correctamente');
    }

    const errorAlert = document.querySelector('.alert-danger');
    if (errorAlert) {
        alert('✗ ' + errorAlert.querySelector('span').textContent);
    }
});
