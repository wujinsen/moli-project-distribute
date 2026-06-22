htps:/8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html

# The Clean Architecture

Uncle Bob

13 Aug 2012

Architecture

Craftsmanship

### Twet

![image 1](<The Clean Architecture.note_images/imageFile1.png>)

posts/2012-08-13-the-clean-architecture/CleanArchitecture.jpg

Over the last several years weʼve sen a whole range of ideas regarding the architecture of systems. These include:

(a.k.a. Ports and Adapters) by Alistair Cockburn and adopted by Steve Freman, and Nat Pryce in their wonderful bok

Hexagonal Architecture

Growing Object Oriented Software

Onion Architecture by Jefrey Palermo

Screaming Architecture from a blog of mine last year

DCI from James Coplien, and Trygve Renskaug.

by Ivar Jacobson from his bok Object Oriented Software Enginering: A Use-Case Driven Aproach

BCE

Though these architectures al vary somewhat in their details, they are very similar. They al have the same objective, which is the separation of concerns. They al achieve this separation by dividing the software into layers. Each has at least one layer for busines rules, and another for interfaces. Each of these architectures produce systems that are:

- 1.
- 2.
- 3.
- 4.
- 5.


Independent of Frameworks. The architecture does not depend on the existence of some library of feature laden software. This alows you to use such frameworks as tols, rather than having to cram your system into their limited constraints. Testable. The busines rules can be tested without the UI, Database, Web Server, or any other external element. Independent of UI. The UI can change easily, without changing the rest of the system. A Web UI could be replaced with a console UI, for example, without changing the busines rules. Independent of Database. You can swap out Oracle or SQL Server, for Mongo, BigTable, CouchDB, or something else. Your busines rules are not bound to the database. Independent of any external agency. In fact your busines rules simply donʼt know anything at al about the outside world.

The diagram at the top of this article is an atempt at integrating al these architectures into a single actionable idea.

## The Dependency Rule

The concentric circles represent diferent areas of software. In general, the further in you go, the higher level the software becomes. The outer circles are mechanisms. The i ner circles are policies. The overiding rule that makes this architecture work is The Dependency Rule. This rule says that source code dependencies can only point inwards. Nothing in an i ner circle can know anything at al about something in an outer circle. In particular, the name of something declared in an outer circle must not be mentioned by the code in an i ner circle. That includes, functions, clases. variables, or any other named software entity. By the same token, data formats used in an outer circle should not be used by an i ner circle, especialy if those formats are generate by a framework in an outer circle. We donʼt want anything in an outer circle to impact the i ner circles.

## Entities

Entities encapsulate Enterprise wide busines rules. An entity can be an object with methods, or it can be a set of data structures and functions. It doesnʼt mater so long as the entities could be used by many diferent aplications in the enterprise. If you donʼt have an enterprise, and are just writing a single aplication, then these entities are the busines objects of the aplication. They encapsulate the most general and high-level rules. They are the least likely to change when something external changes. For example, you would not expect these objects to be afected by a change to page navigation, or security. No operational change to any particular aplication should afect the entity layer.

## Use Cases

The software in this layer contains aplication specific busines rules. It encapsulates and implements al of the use cases of the system. These use cases orchestrate the flow of data to and from the entities, and direct those entities to use their enterprise wide busines rules to achieve the goals of the use case. We do not expect changes in this layer to afect the entities. We also do not expect this layer to be afected by changes to externalities such as the database, the UI, or any of the comon frameworks. This layer is isolated from such concerns. We do, however, expect that changes to the operation of the aplication wil afect the usecases and therefore the software in this layer. If the details of a use-case change, then some code in this layer wil certainly be afected.

## Interface Adapters

The software in this layer is a set of adapters that convert data from the format most convenient for the use cases and entities, to the format most convenient for some external agency such as the Database or the Web. It is this layer, for example, that wil wholy contain the MVC architecture of a GUI. The Presenters, Views, and Controlers al belong in here. The models are likely just data structures that are pased from the controlers to the use cases, and then back from the use cases to the presenters and views. Similarly, data is converted, in this layer, from the form most convenient for entities and use cases, into the form most convenient for whatever persistence framework is being used. i.e. The Database. No code inward of this circle should know anything at al about the database. If the database is a SQL database, then al the SQL should be restricted to this layer, and in particular to the parts of this layer that have to do with the database. Also in this layer is any other adapter necesary to convert data from some external form, such as an external service, to the internal form used by the use cases and entities.

## Frameworks and Drivers.

The outermost layer is generaly composed of frameworks and tols such as the Database, the Web Framework, etc. Generaly you donʼt write much code in this layer other than glue code that comunicates to the next circle inwards. This layer is where al the details go. The Web is a detail. The database is a detail. We kep these things on the outside where they can do litle harm.

## Only Four Circles?

No, the circles are schematic. You may find that you ned more than just these four. Thereʼs no rule that says you must always have just these four. However, The Dependency Rule always aplies. Source code dependencies always point inwards. As you move inwards the level of abstraction increases. The outermost circle is low level concrete detail. As you move inwards the software grows more abstract, and encapsulates higher level policies. The i ner most circle is the most general.

## Crossing boundaries.

At the lower right of the diagram is an example of how we cros the circle boundaries. It shows the Controlers and Presenters comunicating with the Use Cases in the next layer. Note the flow of control. It begins in the controler, moves through the use case, and then winds up executing in the presenter. Note also the source code dependencies. Each one of them points inwards towards the use cases. We usualy resolve this aparent contradiction by using the . In a language like Java, for example, we would arange interfaces and inheritance relationships such that the source code dependencies opose the flow of control at just the right points acros the boundary. For example, consider that the use case neds to cal the presenter. However, this cal must not be direct because that would violate The Dependency Rule: No name in an outer circle can be mentioned by an i ner circle. So we have the use case cal an interface (Shown here as Use Case Output Port) in the i ner circle, and have the presenter in the outer circle implement it. The same technique is used to cros al the boundaries in the architectures. We take advantage of dynamic polymorphism to create source code dependencies that opose the flow of control so that we can conform to The Dependency Rule no mater what direction the flow of control is going in.

Dependency Inversion Principle

## What data crosses the boundaries.

Typicaly the data that croses the boundaries is simple data structures. You can use basic structs or simple Data Transfer objects if you like. Or the data can simply be arguments in function cals. Or you can pack it into a hashmap, or construct it into an object. The important thing is that isolated, simple, data structures are pased acros the boundaries. We donʼt want to cheat and pas Entities or Database rows. We donʼt want the data structures to have any kind of dependency that violates The Dependency Rule. For example, many database frameworks return a convenient data format in response to a query. We might cal this a RowStructure. We donʼt want to pas that row structure inwards acros a boundary. That would violate The Dependency Rulebecause it would force an i ner circle to know something about an outer circle. So when we pas data acros a boundary, it is always in the form that is most convenient for the i ner circle.

## Conclusion

Conforming to these simple rules is not hard, and wil save you a lot of headaches going forward. By separating the software into layers, and conforming to The Dependency Rule, you wil create a system that is intrinsicaly testable, with al the benefits that implies. When any of the external parts of the system become obsolete, like the database, or the web framework, you can replace those obsolete elements with a minimum of fus.

