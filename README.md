# BasicCalculator
BasicCalulator have one method:
```Java
IntegerType calculate(String expression); 
```

BasicCalculator can work with different types (```T```) but they must implements ```Evaluable<T>```.

Now BasicCalculator can calculate expressions with:

* \+
* \- (unary minus can be in parenthesis or in begging of expression)
* ()
* and numbers in 10-radix
