# python-inference-api


Это слегка устаревшая, но рабочая версия веб-сервера на Python, в который можно заворачивать модели и обращаться к ним по  АПИ.

- `Dockerfile` - конфигурация контейнера (вашей виртуальной машины)
- `server.py` - точка входа (aka main) + rest controller, которая запускает веб-сервер, слушает порт и делегирует запросы модели. основан на `cherrypy`
- requirements.txt - ваши зависимости. 
- config.yml - параметра (хост, порт, размер пула, любые другие).


Если умеетет лучше (cherrypy, django) - пулл-реквесты приветствуются.

Сделайте себе репозиторий на основе этого темплейта, и оберните свои модели.

## Результаты работы

Исходное изображение | Результат | 	Время работы, с.
--- | --- | ---
![plot](examples/1.jpg) | ![plot](cutouts/cutout_1.png) | 7.091
![plot](examples/2.jpg) | ![plot](cutouts/cutout_2.png) | 7.297
![plot](examples/3.jpg) | ![plot](cutouts/cutout_3.png) | 6.594
![plot](examples/4.jpg) | ![plot](cutouts/cutout_4.png) | 6.594
![plot](examples/5.jpg) | ![plot](cutouts/cutout_5.png) | 5.844
![plot](examples/6.jpg) | ![plot](cutouts/cutout_6.png) | 5.844
![plot](examples/7.jpg) | ![plot](cutouts/cutout_7.png) | 7.64
![plot](examples/8.jpg) | ![plot](cutouts/cutout_8.png) | 7.125
![plot](examples/9.jpg) | ![plot](cutouts/cutout_9.png) | 7.656
![plot](examples/10.jpg) | ![plot](cutouts/cutout_10.png) | 7.594
![plot](examples/11.jpg) | ![plot](cutouts/cutout_11.png) | 7.281
![plot](examples/12.jpg) | ![plot](cutouts/cutout_12.png) | 7.281
