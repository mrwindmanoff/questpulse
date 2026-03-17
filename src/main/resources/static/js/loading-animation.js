// Анимация загрузки "Лабораторные двери"
(function() {
    // Создаем стили для анимации
    const style = document.createElement('style');
    style.textContent = `
        @keyframes doorGlow {
            0% { box-shadow: 0 0 20px #0ff; }
            50% { box-shadow: 0 0 40px #0ff, 0 0 60px #0ff; }
            100% { box-shadow: 0 0 20px #0ff; }
        }
        
        @keyframes pulse {
            0% { opacity: 0.6; }
            50% { opacity: 1; }
            100% { opacity: 0.6; }
        }
        
        .door-left::before, .door-right::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
        }
        
        .door-left::before {
            background: repeating-linear-gradient(
                45deg,
                transparent,
                transparent 10px,
                rgba(0, 255, 255, 0.1) 10px,
                rgba(0, 255, 255, 0.1) 20px
            );
        }
        
        .door-right::before {
            background: repeating-linear-gradient(
                -45deg,
                transparent,
                transparent 10px,
                rgba(255, 0, 255, 0.1) 10px,
                rgba(255, 0, 255, 0.1) 20px
            );
        }
        
        .loading-dots {
            animation: pulse 1.5s infinite;
        }
    `;
    document.head.appendChild(style);

    let doors = null;
    let progressInterval = null;

    function createDoors() {
        const container = document.createElement('div');
        container.id = 'loading-doors';
        container.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 9999;
            pointer-events: none;
            display: flex;
            justify-content: center;
            align-items: center;
        `;

        // Левая дверь
        const leftDoor = document.createElement('div');
        leftDoor.className = 'door-left';
        leftDoor.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            width: 50%;
            height: 100%;
            background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 50%, #0a0f0f 100%);
            border-right: 4px solid #0ff;
            box-shadow: 0 0 30px rgba(0, 255, 255, 0.5);
            z-index: 10000;
            transition: transform 0.8s cubic-bezier(0.77, 0, 0.175, 1);
            display: flex;
            justify-content: flex-end;
            align-items: center;
            padding-right: 20px;
            box-sizing: border-box;
            animation: doorGlow 2s infinite;
        `;

        // Правая дверь
        const rightDoor = document.createElement('div');
        rightDoor.className = 'door-right';
        rightDoor.style.cssText = `
            position: absolute;
            top: 0;
            right: 0;
            width: 50%;
            height: 100%;
            background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 50%, #0a0f0f 100%);
            border-left: 4px solid #f0f;
            box-shadow: 0 0 30px rgba(255, 0, 255, 0.5);
            z-index: 10000;
            transition: transform 0.8s cubic-bezier(0.77, 0, 0.175, 1);
            display: flex;
            justify-content: flex-start;
            align-items: center;
            padding-left: 20px;
            box-sizing: border-box;
            animation: doorGlow 2s infinite;
        `;

        // Неоновые полосы на дверях
        const leftGlow = document.createElement('div');
        leftGlow.style.cssText = `
            width: 4px;
            height: 80%;
            background: #0ff;
            box-shadow: 0 0 20px #0ff;
            border-radius: 2px;
        `;
        leftDoor.appendChild(leftGlow);

        const rightGlow = document.createElement('div');
        rightGlow.style.cssText = `
            width: 4px;
            height: 80%;
            background: #f0f;
            box-shadow: 0 0 20px #f0f;
            border-radius: 2px;
        `;
        rightDoor.appendChild(rightGlow);

        // Центральный контент
        const centerContent = document.createElement('div');
        centerContent.style.cssText = `
            position: relative;
            z-index: 10001;
            text-align: center;
            color: #0ff;
            font-family: 'Courier New', monospace;
            text-shadow: 0 0 10px #0ff;
            opacity: 1;
            transition: opacity 0.3s ease;
        `;

        // Текст
        const loadingText = document.createElement('div');
        loadingText.style.cssText = `
            font-size: 2rem;
            margin-bottom: 20px;
            letter-spacing: 8px;
            text-transform: uppercase;
            font-weight: bold;
        `;
        loadingText.textContent = 'ЗАГРУЗКА';

        // Анимированные точки
        const dots = document.createElement('div');
        dots.className = 'loading-dots';
        dots.style.cssText = `
            font-size: 3rem;
            letter-spacing: 10px;
            margin-bottom: 30px;
        `;
        dots.textContent = '...';

        // Прогресс-бар
        const progressBar = document.createElement('div');
        progressBar.style.cssText = `
            width: 300px;
            height: 6px;
            background: #1a1f2f;
            border: 2px solid #0ff;
            margin: 20px auto;
            position: relative;
            overflow: hidden;
            box-shadow: 0 0 20px #0ff;
            border-radius: 3px;
        `;

        const progressFill = document.createElement('div');
        progressFill.id = 'door-progress';
        progressFill.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            height: 100%;
            width: 0%;
            background: linear-gradient(90deg, #0ff, #f0f);
            box-shadow: 0 0 30px #0ff;
            transition: width 0.3s ease;
        `;
        progressBar.appendChild(progressFill);

        centerContent.appendChild(loadingText);
        centerContent.appendChild(dots);
        centerContent.appendChild(progressBar);

        container.appendChild(leftDoor);
        container.appendChild(rightDoor);
        container.appendChild(centerContent);

        return {
            container,
            leftDoor,
            rightDoor,
            centerContent,
            progressFill,
            dots,
            loadingText
        };
    }

    // API для управления дверями
    window.doors = {
        close: function() {
            if (!doors) {
                doors = createDoors();
                document.body.appendChild(doors.container);
                
                // Сразу показываем закрытые двери
                doors.leftDoor.style.transform = 'translateX(0)';
                doors.rightDoor.style.transform = 'translateX(0)';
                
                // Анимация точек
                let dotCount = 0;
                if (progressInterval) clearInterval(progressInterval);
                progressInterval = setInterval(() => {
                    if (doors && doors.dots) {
                        dotCount = (dotCount + 1) % 4;
                        doors.dots.textContent = '.'.repeat(dotCount);
                    }
                }, 300);

                // Анимация прогресса
                let progress = 0;
                const progressInterval2 = setInterval(() => {
                    progress += Math.random() * 10;
                    if (progress > 100) progress = 100;
                    if (doors && doors.progressFill) {
                        doors.progressFill.style.width = progress + '%';
                    }
                    if (progress === 100) {
                        clearInterval(progressInterval2);
                    }
                }, 200);
            }
        },
        
        open: function() {
            if (doors) {
                // Открываем двери
                doors.leftDoor.style.transform = 'translateX(-100%)';
                doors.rightDoor.style.transform = 'translateX(100%)';
                doors.centerContent.style.opacity = '0';
                
                // Очищаем интервалы
                if (progressInterval) {
                    clearInterval(progressInterval);
                    progressInterval = null;
                }
                
                // Удаляем контейнер после анимации
                setTimeout(() => {
                    if (doors && doors.container.parentNode) {
                        doors.container.parentNode.removeChild(doors.container);
                        doors = null;
                    }
                }, 800);
            }
        },
        
        setMessage: function(msg) {
            if (doors && doors.loadingText) {
                doors.loadingText.textContent = msg;
            }
        }
    };

    // Показываем двери при начале загрузки страницы
    if (document.readyState === 'loading') {
        window.doors.close();
    }

    // Скрываем двери когда страница полностью загружена
    window.addEventListener('load', function() {
        setTimeout(() => {
            window.doors.open();
        }, 500);
    });

    // Перехватываем переходы по ссылкам
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.href && !link.href.includes('#') && !link.target) {
            const currentDomain = window.location.origin;
            if (link.href.startsWith(currentDomain) && !link.href.includes('logout')) {
                e.preventDefault();
                window.doors.close();
                setTimeout(() => {
                    window.location.href = link.href;
                }, 300);
            }
        }
    });

    // Для кнопок с formaction (как logout)
    document.addEventListener('submit', function(e) {
        const form = e.target;
        if (form.method === 'post' && form.action && form.action.includes('logout')) {
            window.doors.close();
        }
    });
})();