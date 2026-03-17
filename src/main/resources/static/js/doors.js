// Простая и надёжная анимация дверей
(function() {
    // Создаём двери сразу при загрузке скрипта
    const doorsContainer = document.createElement('div');
    doorsContainer.id = 'doors-animation';
    doorsContainer.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 999999;
        pointer-events: none;
        display: flex;
    `;

    // Левая дверь
    const leftDoor = document.createElement('div');
    leftDoor.id = 'left-door-animation';
    leftDoor.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 50%;
        height: 100%;
        background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 100%);
        border-right: 4px solid #0ff;
        box-shadow: 0 0 30px #0ff;
        transition: transform 0.8s ease-in-out;
        transform: translateX(0);
        z-index: 999999;
    `;

    // Правая дверь
    const rightDoor = document.createElement('div');
    rightDoor.id = 'right-door-animation';
    rightDoor.style.cssText = `
        position: absolute;
        top: 0;
        right: 0;
        width: 50%;
        height: 100%;
        background: linear-gradient(135deg, #1a1f2f 0%, #0a0f0f 100%);
        border-left: 4px solid #f0f;
        box-shadow: 0 0 30px #f0f;
        transition: transform 0.8s ease-in-out;
        transform: translateX(0);
        z-index: 999999;
    `;

    // Текст посередине
    const centerText = document.createElement('div');
    centerText.id = 'doors-text';
    centerText.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: #0ff;
        font-family: 'Courier New', monospace;
        font-size: 2rem;
        text-shadow: 0 0 10px #0ff;
        z-index: 1000000;
        text-align: center;
    `;
    centerText.innerHTML = 'ЗАГРУЗКА<span id="doors-dots">...</span>';

    doorsContainer.appendChild(leftDoor);
    doorsContainer.appendChild(rightDoor);
    doorsContainer.appendChild(centerText);

    // Добавляем двери в body сразу
    if (document.body) {
        document.body.appendChild(doorsContainer);
    } else {
        document.addEventListener('DOMContentLoaded', function() {
            document.body.appendChild(doorsContainer);
        });
    }

    // Анимация точек
    let dotsCount = 0;
    setInterval(function() {
        const dots = document.getElementById('doors-dots');
        if (dots) {
            dotsCount = (dotsCount + 1) % 4;
            dots.textContent = '.'.repeat(dotsCount);
        }
    }, 300);

    // Функция для открытия дверей
    window.openDoors = function() {
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        
        if (left && right) {
            left.style.transform = 'translateX(-100%)';
            right.style.transform = 'translateX(100%)';
            if (text) {
                setTimeout(() => {
                    text.style.opacity = '0';
                }, 200);
            }
            
            // Удаляем двери через 1 секунду
            setTimeout(() => {
                const container = document.getElementById('doors-animation');
                if (container) {
                    container.style.display = 'none';
                }
            }, 1000);
        }
    };

    // Функция для закрытия дверей
    window.closeDoors = function() {
        const container = document.getElementById('doors-animation');
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        
        if (container) {
            container.style.display = 'flex';
        }
        if (left && right) {
            left.style.transform = 'translateX(0)';
            right.style.transform = 'translateX(0)';
            if (text) {
                text.style.opacity = '1';
            }
        }
    };

    // Автоматически открываем двери после загрузки
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            setTimeout(window.openDoors, 500);
        });
    } else {
        setTimeout(window.openDoors, 500);
    }

    // Перехватываем переходы по ссылкам
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.href && !link.href.includes('#')) {
            const isSameDomain = link.href.startsWith(window.location.origin);
            const isNotLogout = !link.href.includes('logout');
            
            if (isSameDomain && isNotLogout) {
                e.preventDefault();
                window.closeDoors();
                setTimeout(() => {
                    window.location.href = link.href;
                }, 300);
            }
        }
    });

    // Перехватываем отправку форм
    document.addEventListener('submit', function(e) {
        window.closeDoors();
    });
})();