# android-littlefinger
Android fingerprint library - это библиотека для упрощения процесса использования сканера отпечатка пальцев на Android.

## Добавление в проект

Через gradle - зависимость
```groovy 
implementation 'com.elegion.littlefinger:littlefinger:0.9.0'
```

## Пример использования

Асинхронное шифрование текста и с заданным ключом

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

