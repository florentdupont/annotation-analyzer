# Getting Started

Vérifie via un TU qu'une méthode qui lance une BusinessException est bien annotée avec une @ThrowsBE(code), dont le code correspond au code utilisé lors de la création de la BusinessException.

Si le code de l'annotation ne correspond pas, alors le Parser leve une exception!

Par exemple : 
```
@ThrowsBE("123")
void service() throws BusinessException {

  throw new BusinessException("123")

}


```
