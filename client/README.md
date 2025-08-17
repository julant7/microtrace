Сервис для добавления трейсов

Для подключения к OpenTelemetry добавить переменные среды
```
OTEL_SERVICE_NAME="<service_name>";
OTEL_EXPORTER_OTLP_ENDPOINT="http://<host>:<port>";
OTEL_LOGS_EXPORTER=otlp;
OTEL_TRACES_EXPORTER=otlp;
OTEL_METRICS_EXPORTER=otlp
```
