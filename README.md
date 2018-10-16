# Online Store Kata

The objective of this kata is to practice with functional programming in Java with [vavr](http://www.vavr.io/) and [Resilience4j](http://resilience4j.github.io/resilience4j/).

You have been hired to develop an application for Acme Corporation. Since they have a large history of unsuccessful projects, you are required to design a robust solution for their main business (online sales of heavily over-engineered products for road runner hunting). As a developer, you are supposed to develop a solution that move the orders from the online store to the site of the company. Orders can have duplicate items that you should remove before recording a file in the ERP and inserting a record into the database.

You communicate with the ERP via a shared directory where you have to write the order. Every order produces a log message in the logging system, and summary log is written at the end of each execution.

You are warned to beware of the frequent tendency of the Internet link to disconnect from the online store. Every time the Internet connection went down, the subsequent messages sent to the online store causes additional overhead in the link, delaying its recovery.

It's a completely different story when the application fails to write a file in the filesystem shared with the ERP. In such a case, retrying the same operation after a few seconds will do the job.

![Example 1](https://raw.githubusercontent.com/etorres/online-store-kata-vavr/master/images/online-store.png "Example 1")

## Rules for the kata

`OrderProcessor` class provides an skeleton of the application with a few annotations to guide your work.

Additionally, you're provided with an acceptance test: `OrderProcessorTest`. Write any other test of any kind that you consider necessary for the kata.

## A 5-minute introduction to vavr

`Try` is a data type used in [vavr](http://www.vavr.io/vavr-docs/#_side_effects) to represent side-effects.

You can compose functions with `andThen` or `compose`: [vavr](http://www.vavr.io/vavr-docs/#_composition).

`Match` is a pattern matching with support for object decomposition and that can be used as a statement or expression: [vavr](http://www.vavr.io/vavr-docs/#_pattern_matching)

In you have one minute left, please read the links above.

## Feedback after session in Mango

1. Most people with Windows laptops have problems with Docker. Using an in-memory database like H2 may help to avoid these issues.
2. Removing duplicate orders from the list is made via a database batch update operation. This is an optimization, not suitable for a kata.
3. Summary log is showing 0 order processed of 0 possible when a file writing fails.
4. Steps: Acceptance tests, List operations, Order operations, failure recovery.
