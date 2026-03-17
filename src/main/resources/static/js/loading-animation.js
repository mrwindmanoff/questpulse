// Анимация загрузки в стиле "лабораторные двери"
(function() {
    // Создаем элементы анимации
    function createLoadingAnimation() {
        // Контейнер для анимации
        const loadingContainer = document.createElement('div');
        loadingContainer.id = 'loading-animation';
        loadingContainer.style.cssText = `
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
        leftDoor.id = 'left-door';
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
        `;

        // Правая дверь
        const rightDoor = document.createElement('div');
        rightDoor.id = 'right-door';
        rightDoor.style.cssText = `
            position: absolute;
            top: 0;
            right: 0;
            width: 50%;
            height: 100%;
            background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 50%, #0a0f0f 100%);
            border-left: 4px solid #0ff;
            box-shadow: 0 0 30px rgba(0, 255, 255, 0.5);
            z-index: 10000;
            transition: transform 0.8s cubic-bezier(0.77, 0, 0.175, 1);
            display: flex;
            justify-content: flex-start;
            align-items: center;
            padding-left: 20px;
            box-sizing: border-box;
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

        // Центральный элемент с загрузкой
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

        // Текст загрузки
        const loadingText = document.createElement('div');
        loadingText.style.cssText = `
            font-size: 1.5rem;
            margin-bottom: 20px;
            letter-spacing: 4px;
            text-transform: uppercase;
        `;
        loadingText.textContent = 'Загрузка';

        // Анимированные точки
        const dots = document.createElement('div');
        dots.style.cssText = `
            font-size: 2rem;
            letter-spacing: 8px;
        `;
        dots.textContent = '...';

        // Прогресс-бар
        const progressBar = document.createElement('div');
        progressBar.style.cssText = `
            width: 200px;
            height: 4px;
            background: #1a1f2f;
            border: 2px solid #0ff;
            margin: 20px auto;
            position: relative;
            overflow: hidden;
            box-shadow: 0 0 15px #0ff;
        `;

        const progressFill = document.createElement('div');
        progressFill.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            height: 100%;
            width: 0%;
            background: #0ff;
            box-shadow: 0 0 20px #0ff;
            transition: width 0.3s ease;
        `;
        progressBar.appendChild(progressFill);

        centerContent.appendChild(loadingText);
        centerContent.appendChild(dots);
        centerContent.appendChild(progressBar);

        loadingContainer.appendChild(leftDoor);
        loadingContainer.appendChild(rightDoor);
        loadingContainer.appendChild(centerContent);

        return {
            container: loadingContainer,
            leftDoor: leftDoor,
            rightDoor: rightDoor,
            centerContent: centerContent,
            progressFill: progressFill,
            dots: dots
        };
    }

    // Управление анимацией
    const animation = createLoadingAnimation();
    let isVisible = false;
    let progressInterval = null;

    window.loadingAnimation = {
        show: function() {
            if (!isVisible) {
                document.body.appendChild(animation.container);
                isVisible = true;
                
                // Анимация точек
                let dotCount = 0;
                if (progressInterval) clearInterval(progressInterval);
                progressInterval = setInterval(() => {
                    if (animation.dots) {
                        dotCount = (dotCount + 1) % 4;
                        animation.dots.textContent = '.'.repeat(dotCount);
                    }
                }, 300);

                // Анимация прогресс-бара (для демонстрации)
                let progress = 0;
                const progressInterval2 = setInterval(() => {
                    progress += Math.random() * 15;
                    if (progress > 100) progress = 100;
                    if (animation.progressFill) {
                        animation.progressFill.style.width = progress + '%';
                    }
                    if (progress === 100) {
                        clearInterval(progressInterval2);
                    }
                }, 200);
            }
        },
        
        hide: function() {
            if (isVisible) {
                // Открываем двери
                animation.leftDoor.style.transform = 'translateX(-100%)';
                animation.rightDoor.style.transform = 'translateX(100%)';
                animation.centerContent.style.opacity = '0';
                
                // Очищаем интервалы
                if (progressInterval) {
                    clearInterval(progressInterval);
                    progressInterval = null;
                }
                
                // Удаляем контейнер после анимации
                setTimeout(() => {
                    if (animation.container.parentNode) {
                        animation.container.parentNode.removeChild(animation.container);
                    }
                    isVisible = false;
                    // Сбрасываем позиции дверей
                    animation.leftDoor.style.transform = '';
                    animation.rightDoor.style.transform = '';
                    animation.centerContent.style.opacity = '1';
                    if (animation.progressFill) animation.progressFill.style.width = '0%';
                }, 800);
            }
        },
        
        updateProgress: function(percent) {
            if (animation.progressFill) {
                animation.progressFill.style.width = percent + '%';
            }
        },
        
        setMessage: function(message) {
            if (animation.centerContent) {
                const loadingText = animation.centerContent.children[0];
                if (loadingText) loadingText.textContent = message;
            }
        }
    };

    // Автоматически показывать анимацию при загрузке страницы
    document.addEventListener('DOMContentLoaded', function() {
        window.loadingAnimation.show();
        
        // Скрываем анимацию после полной загрузки
        window.addEventListener('load', function() {
            setTimeout(() => {
                window.loadingAnimation.hide();
            }, 800); // Даем время насладиться анимацией
        });
    });

    // Показывать анимацию при переходе по ссылкам
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.href && !link.href.includes('#') && !link.target) {
            const currentDomain = window.location.origin;
            if (link.href.startsWith(currentDomain)) {
                e.preventDefault();
                window.loadingAnimation.show();
                setTimeout(() => {
                    window.location.href = link.href;
                }, 300);
            }
        }
    });
})();