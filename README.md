# AnotacaoBuilderJava

```java
Esse pacote existe por fins educacionais e não foi feito com a intenção de ser usado para desenvolver sistemas.

Esse pacote define uma anotação que criar Builders automaticamente a partir de construtores.

class Exemplo {
    @AutoBuilder
    public Exemplo(String foo, boolean bar) {}
}

Essa anotação gera:

class ExemploBuilder {
    private String _foo;
    private boolean _bar;

    ExemploBuilder foo(String foo) {
        this._foo = foo;
        return this;
    }

    ExemploBuilder bar(String bar) {
        this._foo = foo;
        return this;
    }

    Exemplo build() {
        return new Exemplo(this._foo, this._bar);
    }
}
```