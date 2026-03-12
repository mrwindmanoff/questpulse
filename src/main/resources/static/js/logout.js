// Функция для удаления куки по имени
function eraseCookie(name) {
    document.cookie = name + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    document.cookie = name + '=; Path=/; Domain=' + window.location.hostname + '; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

// Функция для удаления всех меток устройства
function clearDeviceMarks() {
    // Удаляем из localStorage
    localStorage.removeItem('questpulse_device_id');
    
    // Удаляем куки (несколько вариантов для надёжности)
    eraseCookie('questpulse_device_id');
    eraseCookie('device_id');
    
    console.log('Device marks cleared');
}

// Выполняем при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Проверяем, что мы на странице выхода
    if (window.location.pathname === '/logout-success' || window.location.pathname === '/logout') {
        clearDeviceMarks();
    }
});

// Дополнительная очистка при уходе со страницы (на всякий случай)
window.addEventListener('beforeunload', function() {
    if (window.location.pathname === '/logout-success' || window.location.pathname === '/logout') {
        clearDeviceMarks();
    }
});