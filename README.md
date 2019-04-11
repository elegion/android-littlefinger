# android-littlefinger
LittleFinger предназначен для упрощения работы со сканером отпечатков пальцев на Android. С помощью LittleFinger можно:

- [выяснить, есть ли на устройстве возможность использования сенсора](../../wiki/Проверка-наличия-и-состояния-сенсора)
- [провести холостой прогон сенсора, просто поверив, что текущего пользователя можно распознать](../../wiki/Холостой-прогон-сенсора)
- [зашифровать публичным \ расшифровать приватным ключом](../../wiki/Криптография)
- [зашифровать \ расшифровать секретным ключом](../../wiki/Криптография)

## Добавление в проект

Через gradle - зависимость
```groovy 
implementation 'com.elegion.library.littlefinger:littlefinger:0.9.0'
```

## Пример использования

В конструктор нужно передавать контекст 
```java
LittleFinger mLittlefinger = new LittleFinger(getApplicationContext());
```

### Асинхронное шифрование текста с заданным ключом

```java
 mLittleFinger.encode(pin, key, CryptoAlgorithm.RSA, this::handleResult);
```

Обработка результата

```java
private void handleResult(AuthResult authResult) {
        switch (authResult.getState()) {
            case SUCCESS: {
                String encoded = authResult.getData();
                mRepository.saveEncodedPin(encoded);
                getViewState().showEncodedPin(encoded);
                break;
            }
            default:
                if (authResult.getThrowable() != null) {
                    getViewState().showEncodedPin(authResult.getThrowable().getMessage());
                }
                break;
        }
    }
  ```
  
  ### Расшифровка 
  ```java
   String encoded = mRepository.getEncodedPin();
   String key = mRepository.getKeyAlias();
   mLittleFinger.decode(encoded, key, CryptoAlgorithm.RSA, this::handleCallback);
```

Обработка результата

```java
private void handleCallback(AuthResult result) {
        switch (result.getState()) {
            case SUCCESS:
                checkIsPinCorrect(result.getData());
                break;
            case HELP:
                getViewState().showMessage(R.string.fp_touch_help);
                break;
            case ERROR:
                getViewState().showMessage(R.string.fp_touch_failed);
                break;
            case EXCEPTION:
                if (result.isKeyInvalidated()) {
                    getViewState().setSensorStateMessage(R.string.fp_added_or_removed_fp);
                    mRepository.saveEncodedPin(null);
                } else {
                    getViewState().showMessage(R.string.fp_auth_by_fp_unavailable);
                }
                Log.d(TAG, "exc", result.getThrowable());
                break;
        }
    }
```
