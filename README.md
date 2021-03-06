When you want some atomic tasks to be executed one after another following a specific workflow, and when you want those  processed tasks to be cancelled if, at some point in the workflow, an unexpected error occurs, then this set of classes may help you.

Note : this project is very much inspired by [jPos](http://www.jpos.org/) monetic framework's [transaction manager concepts](http://www.andyorrock.com/2007/02/the_jpos_transa.html). It intends not to be an alternative nor a concurrent to the jPos transaction manager which works just fine. Rather, it provides some set of classes that are candidate to dependency injection through an IOC container (like for instance [Spring](http://docs.spring.io/spring/docs/3.0.x/reference/beans.html)).

<h2>Adding txnmgr to a Maven project</h2>
Add the following dependency section to your pom.xml :
```
<dependency>
    <groupId>com.github.dgrandemange</groupId>
    <artifactId>txnmgr</artifactId>
    <version>1.0.0</version>
</dependency>
```

<h2>Recipee</h2>
First, think about your workflow and what should be done from a business point of view. Doing so, you should be able to identify the different tasks you need. We will call theses tasks <i>participants</i>.
A <i>participant</i> has to be an implementation of the <code>fr.dgrandemange.txnmgr.service.IParticipant</code> interface.

In your workflow, there may be different ways for the job to be done, depending on the execution context. So, at some points in the workflow, some paths will be <i>selected</i> and some others won't.

So how is the selection done ? <br>
Well, when a participant is processed (see method <code>fr.dgrandemange.txnmgr.service.IParticipant.execute(IContextMgr contextMgr)</code>), it can returns a space delimited list of <i>group</i> names. A <i>group</i> is an ordered list of <i>participants</i>. <br>
When a <i>group</i> is selected, its participants are executed one after another, following the order of their declaration in the <i>group</i>.

All <i>groups</i> must be registered in a <code>fr.dgrandemange.txnmgr.model.ParticipantsGroupRegistry</code> and uniquely identified by a name.

The transaction is handled by a <i>transaction manager</i>. A basic implementation is provided with the <code>fr.dgrandemange.txnmgr.service.support.TransactionMgrImpl</code> class.

See a sample usage in this [unit test](/src/test/java/fr/dgrandemange/txnmgr/service/support/TransactionMgrImplTest.java).

<h2>Related projects</h2>
The [txnmgr-springframework-ext](https://github.com/dgrandemange/txnmgr-springframework-ext) project provides a nice way to configure (definition and external dependencies injection) participants (and groups of) through a Spring XML application context configuration.

The [jPos Workflow Eclipse plugin](https://github.com/dgrandemange/jPosWorkflowEclipsePlugin) project which provides a way to view a transaction workflow XML configuration as a directed graph under the Eclipse IDE.
