# Try Annotation Processor

A project to explore how annotation processors work.
Currently, it includes the following:
- a `@Mapper` annotation that is heavily inspired by [MapStruct](https://mapstruct.org)
- a check code smell validator, inspired by [Error Prone](https://errorprone.info) 
- a `@ToString` annotation, that roughly mimics the one of [Project Lombok](https://projectlombok.org/)
- an `@NullPropagates` parameter annotation, comparable to `https://projectlombok.org/features/NonNull`, but short-circuits the method with `null` when the argument is `null`
