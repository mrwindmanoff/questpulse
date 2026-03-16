function updateOnlineCounter() {
    fetch('/api/online-count')
        .then(response => response.text())
        .then(count => {
            const counterElement = document.getElementById('online-counter');
            if (counterElement) {
                counterElement.textContent = count;
            }
        })
        .catch(error => console.error('Error fetching online count:', error));
}

document.addEventListener('DOMContentLoaded', function() {
    updateOnlineCounter();
    setInterval(updateOnlineCounter, 30000); // обновляем каждые 30 секунд
});