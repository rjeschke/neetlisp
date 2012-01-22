# neetlisp - A Lisp dialect for the JVM
Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>  
See LICENSE.txt (Apache v2.0) for licensing information.

# What?
Neetlisp is a Lisp dialect (Lisp-1) running on the Java Virtual Machine. Well, I think
it is closer to clojure than to Lisp.

# Why?
There's clojure, right? Well, neetlisp is just a proof of concept. I just wanted
to check out if I can build a bytecode compiling Lisp for the JVM. 

# State?
Somewhere between alpha and beta, there's a lot of stuff missing.

# How?
Clone from github and run ant. This will open up the Java SWING neetlisp REPL. You may also
have a look at src/neetlisp/nlsp for some neetlisp code.

# Features?
There's some hardcore coding action going on to make neetlisp feature complete.
What I got so far is:

* Neat REPL with rainbow-braces
* Reader macros, macros, fns
* basic sequence stuff
* lazy sequences (not 100% complete, about 20% I think)
* Unoptimized bytecode compilation
* Lexical scoping (closures)
* Very basic SWANK implementation (emax-rex) with just connect and eval

Missing/TODO/thoughts:

* loop/recur, tail-calls, lazy-seq
* Improved sequence implementation (Java-side and nlsp-api)
* More sanity checks
* Optimizing compiler
* Java interop?
* More API stuff
* Maps and sets
* Improve SWANK
* Create neetlisp-mode
* More detailed doc-strings
* Code cleanups (especially the compiler section)
* ...

# REPL
Keybindings:

* C-Enter : eval
* C-Escape : clear input
* C-Up : History browse up
* C-Down : History browse down
* C-Backspace : Clear output
* C-+ : Increase font size
* C-- : Decrease font size
* C-l : insert lambda symbol

# Api

See api.html or <http://rjeschke.github.com/neetlisp>.

# neetlisp uses ASM4:

Copyright (c) 2000-2011 INRIA, France Telecom
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holders nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
THE POSSIBILITY OF SUCH DAMAGE.


***

Project link: <https://github.com/rjeschke/neetlisp>
