// Анимация длинных дверей с плавным открытием и закрытием
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
        visibility: hidden;
        perspective: 2000px;
    `;

    // Левая дверь (очень длинная - 70% экрана)
    const leftDoor = document.createElement('div');
    leftDoor.id = 'left-door-animation';
    leftDoor.style.cssText = `
        position: absolute;
        top: -50%;
        left: 0;
        width: 50%;
        height: 200%;
        background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 50%, #0a0f0f 100%);
        border-right: 8px solid #0ff;
        box-shadow: 0 0 50px #0ff, 0 0 100px #0ff inset;
        transition: transform 2s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        transform: translateX(-100%) translateY(-25%);
        z-index: 999999;
        transform-origin: left center;
    `;

    // Правая дверь (очень длинная - 70% экрана)
    const rightDoor = document.createElement('div');
    rightDoor.id = 'right-door-animation';
    rightDoor.style.cssText = `
        position: absolute;
        top: -50%;
        right: 0;
        width: 50%;
        height: 200%;
        background: linear-gradient(135deg, #1a1f2f 0%, #0a0f0f 50%, #1a1f2f 100%);
        border-left: 8px solid #f0f;
        box-shadow: 0 0 50px #f0f, 0 0 100px #f0f inset;
        transition: transform 2s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        transform: translateX(100%) translateY(-25%);
        z-index: 999999;
        transform-origin: right center;
    `;

    // Неоновые линии на дверях
    const leftGlowLines = document.createElement('div');
    leftGlowLines.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: repeating-linear-gradient(
            45deg,
            transparent,
            transparent 20px,
            rgba(0, 255, 255, 0.1) 20px,
            rgba(0, 255, 255, 0.1) 40px
        );
        pointer-events: none;
    `;
    leftDoor.appendChild(leftGlowLines);

    const rightGlowLines = document.createElement('div');
    rightGlowLines.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: repeating-linear-gradient(
            -45deg,
            transparent,
            transparent 20px,
            rgba(255, 0, 255, 0.1) 20px,
            rgba(255, 0, 255, 0.1) 40px
        );
        pointer-events: none;
    `;
    rightDoor.appendChild(rightGlowLines);

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
        font-size: 3rem;
        text-shadow: 0 0 20px #0ff, 0 0 40px #0ff;
        z-index: 1000000;
        text-align: center;
        opacity: 0;
        transition: opacity 0.5s ease;
        background: rgba(10, 15, 15, 0.8);
        padding: 2rem 4rem;
        border: 4px solid #0ff;
        box-shadow: 0 0 50px #0ff;
        letter-spacing: 10px;
    `;
    centerText.innerHTML = 'ЗАГРУЗКА<div style="font-size: 2rem; margin-top: 1rem;" id="doors-dots">...</div>';

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

    // Функция для открытия дверей (плавно убираем в стороны)
    window.openDoors = function() {
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        const container = document.getElementById('doors-animation');
        
        if (left && right) {
            // Плавно открываем двери (убираем в стороны)
            left.style.transform = 'translateX(-150%) translateY(-25%)';
            right.style.transform = 'translateX(150%) translateY(-25%)';
            if (text) {
                // Прячем текст
                setTimeout(() => {
                    text.style.opacity = '0';
                }, 200);
            }
            
            // Скрываем контейнер после завершения анимации
            setTimeout(() => {
                if (container) {
                    container.style.visibility = 'hidden';
                }
            }, 2000);
        }
    };

    // Функция для закрытия дверей (плавно выезжают)
    window.closeDoors = function() {
        const container = document.getElementById('doors-animation');
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        
        if (container) {
            container.style.visibility = 'visible';
        }
        
        if (left && right) {
            // Показываем текст
            if (text) {
                text.style.opacity = '1';
            }
            
            // Закрываем двери (выезжают к центру)
            left.style.transform = 'translateX(0) translateY(-25%)';
            right.style.transform = 'translateX(0) translateY(-25%)';
        }
    };

    // При загрузке страницы двери открыты
    setTimeout(() => {
        window.openDoors();
    }, 100);

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
                }, 2000);
            }
        }
    });

    // Перехватываем отправку форм
    document.addEventListener('submit', function(e) {
        const form = e.target;
        if (!form.action || !form.action.includes('logout')) {
            window.closeDoors();
        }
    });

    // Для кнопки назад в браузере
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            window.openDoors();
        }
    });

    window.addEventListener('load', function() {
        setTimeout(() => {
            window.openDoors();
        }, 500);
    });
})();