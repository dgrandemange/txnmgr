When you want some atomic tasks to be executed one after another following a specific workflow, and when you want those  processed tasks to be cancelled when, at some point in the workflow, an unexpected error occurs, then this set of classes may help you.

Note : this project is very much inspired by [jPos](http://www.jpos.org/) monetic framework's [transaction manager concepts](http://www.andyorrock.com/2007/02/the_jpos_transa.html). It intends not to be an alternative nor a concurrent to the jPos transaction manager (which is excellent). Rather, it provides some set of classes that are more candidate to dependency injection through a IOC container like [Spring](http://docs.spring.io/spring/docs/3.0.x/reference/beans.html) (jPos transaction manager is indeed tightly closed to jPos 's own container Q2).

<h2>Recipee</h2>
First think about your workflow and what should be done from a business point of view. Doing so, you should be able to identify the different tasks you need to code. We call theses tasks <i>participants</i>.
A <i>participant</i> is an implementation of the <code>fr.dgrandemange.txnmgr.service.IParticipant</code> interface.

In your workflow, there may be different ways for the job to be done, depending on the execution context. 

When a participant is processed (see method <code>fr.dgrandemange.txnmgr.service.IParticipant.execute(IContextMgr contextMgr)</code>), it can returns a space delimited list of <i>group</i> names (this is about <i>group(s) selection</i>).<br>
A <i>group</i> is an ordered list of <i>participants</i>. When a <i>group</i> is selected, its embedded participants are executed, following the order of their declaration in this <i>group</i>.

All <i>groups</i> must be registered in a <code>fr.dgrandemange.txnmgr.model.ParticipantsGroupRegistry</code> and uniquely identified by their name.

See also the [txnmgr-springframework-ext](https://github.com/dgrandemange/txnmgr-springframework-ext) project which provides a way to define (and to inject external dependencies into) participants and groups through a Spring factory, using a XML application context configuration.
