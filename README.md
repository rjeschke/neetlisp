# neetlisp - A Lisp dialect for the JVM
Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>  
See LICENSE.txt (Apache v2.0) for licensing information.

# What?
Neetlisp is a Lisp dialect running on the Java Virtual Machine. Well, I think
it is closer to clojure than to Lisp.

# Why?
There's clojure, right? Well, neetlisp is just a proof of concept. I just wanted
to check out if I can build a bytecode compiling Lisp for the JVM. 

# State?
Somewhere between alpha and beta, there's a lot of stuff missing.

# How?
Clone from github and run ant. This will open up the Java SWING neetlisp REPL.

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

This is a dump of all fn docs. Work in progress, some docs missing, lot of
api functions missing.

core/*  
---
fn 'core/* (&)  
(& xs)
 Returns the product of xs, (*) returns 1  

core/+  
---
fn 'core/+ (&)  
(& xs)
 Returns the sum of all xs, (+) returns 0  

core/-  
---
fn 'core/- (1 &)  
(num & nums)
 Returns all nums subtracted from num or the negation of num if no nums are supplied  

core//  
---
fn 'core// (1 &)  
(num & den)
 Returns num divided by all dens, if no dens are supplied
 returns 1/num  

core/<  
---
fn 'core/< (1 &)  
(num & nums)
 Returns true if num & nums are in monotonically increasing order  

core/<=  
---
fn 'core/<= (1 &)  
(num & nums)
 Returns true if num & nums are in monotonically non-decreasing order  

core/=  
---
fn 'core/= (1 &)  
(num & nums)
 Returns true if num is equal to all nums  

core/==  
---
fn 'core/== (1 &)  
(x & xs)
 Returns true if x is equal to all xs  

core/>  
---
fn 'core/> (1 &)  
(num & nums)
 Returns true if num & nums are in monotonically decreasing order  

core/>=  
---
fn 'core/>= (1 &)  
(num & nums)
 Returns true if num & nums are in monotonically non-increasing order  

core/add-search-path  
---
fn 'core/add-search-path (2)  
(key path)
 Adds path to file search path list bound to key  

core/apply  
---
fn 'core/apply (2)  
A doc string  

core/assert  
---
fn 'core/assert (2)  
(str val)
 Throws an exception with message str if val evals to false  

core/bool?  
---
fn 'core/bool? (1)  
(val)
 Returns true if val is a boolean  

core/car  
---
fn 'core/car (1)  
A doc string  

core/cdr  
---
fn 'core/cdr (1)  
A doc string  

core/char  
---
fn 'core/char (1)  
(num)
 Returns num as a char  

core/compile-eval  
---
fn 'core/compile-eval (1)  
(str)
 Parses, compiles and evals str, returns the eval result  

core/complement  
---
fn 'core/complement (1)  
nil  

core/cons  
---
fn 'core/cons (2)  
A doc string  

core/constantly  
---
fn 'core/constantly (1)  
nil  

core/count  
---
fn 'core/count (1)  
A doc string  

core/dec  
---
fn 'core/dec (1)  
nil  

core/def  
---
fn 'core/def (2)  
(name val)
 Evals val and assigns it to name  

core/defmacro  
---
fn 'core/defmacro (2 &)  
(name body & more)
 Defines a macro  

core/defn  
---
macro 'core/defn (1 &)  
nil  

core/do  
---
fn 'core/do (1 &)  
(& body)
 Evaluates all elements of body in order, returns
 the result of the last evaluation  

core/do-seq  
---
macro 'core/do-seq (2)  
nil  

core/doc  
---
fn 'core/doc (1)  
(name)
 Displays the doc-string for 'name  

core/double  
---
fn 'core/double (1)  
(num)
 Returns num as a double  

core/eval  
---
fn 'core/eval (1)  
(expr)
 Evaluates expr, returns the result  

core/filter  
---
fn 'core/filter (2)  
A doc string  

core/float  
---
fn 'core/float (1)  
(num)
 Returns num as a float  

core/fn  
---
fn 'core/fn (2 &)  
([name] [doc-string] binds & body)
 Defines a function with optional name and/or doc-string  

core/get  
---
fn 'core/get (2 &)  
A doc string  

core/identity  
---
fn 'core/identity (1)  
nil  

core/if  
---
fn 'core/if (3)  
(cond then else)
 If cond evals to true evals then otherwise else  

core/if-not  
---
macro 'core/if-not (3)  
nil  

core/inc  
---
fn 'core/inc (1)  
nil  

core/int  
---
fn 'core/int (1)  
(num)
 Returns num as an int  

core/iterate  
---
fn 'core/iterate (2)  
A doc string  

core/let  
---
fn 'core/let (2 &)  
(binds body & more)
 Evals body&more with the specified local bindings  

core/list  
---
fn 'core/list (&)  
A doc string  

core/load-file  
---
fn 'core/load-file (1)  
(path)
 Loads and evaluates a file, path may be a name or a string  

core/long  
---
fn 'core/long (1)  
(num)
 Returns num as a long  

core/macroexpand  
---
fn 'core/macroexpand (1)  
(sexp)
 Returns the result of the macro expansion in seq  

core/map  
---
fn 'core/map (2)  
A doc string  

core/max  
---
fn 'core/max (1 &)  
(num & nums)
 Returns the maximum of num & nums  

core/min  
---
fn 'core/min (1 &)  
(num & nums)
 Returns the minimum of num & nums  

core/next  
---
fn 'core/next (1)  
nil  

core/nil?  
---
fn 'core/nil? (1)  
(val)
 Returns true if val is nil  

core/not  
---
fn 'core/not (1)  
(val)
 Returns the logical complement of val  

core/ns  
---
fn 'core/ns (1)  
(name)
 Change current namespace to name  

core/number?  
---
fn 'core/number? (1)  
(val)
 Returns true if val is a number  

core/partial  
---
fn 'core/partial (2)  
nil  

core/prn  
---
fn 'core/prn (&)  
(& xs)
 Prints all xs with a newline after all xs  

core/quote  
---
fn 'core/quote (1)  
(val)
 Returns val as-is without evaluation  

core/range  
---
fn 'core/range (&)  
A doc string  

core/reduce  
---
fn 'core/reduce (2 &)  
A doc string  

core/reverse  
---
fn 'core/reverse (1)  
A doc string  

core/seq  
---
fn 'core/seq (1)  
A doc string  

core/str  
---
fn 'core/str (&)  
(& xs)
 Returns a string consisting of the concatenations of all xs, nil results
 in an empty string  

core/string?  
---
fn 'core/string? (1)  
(val)
 Returns true if val is a string  

core/syntax-quote  
---
fn 'core/syntax-quote (1)  
(val)
 Special quote which allows the use of unquote inside of val,
 mainly used in macros  

core/take  
---
fn 'core/take (2)  
A doc string  

core/take-all  
---
fn 'core/take-all (1)  
A doc string  

core/unquote  
---
fn 'core/unquote (1)  
(val)
 Unquotes val, only accessible in syntax-quote  

core/unquote-splice  
---
fn 'core/unquote-splice (1)  
(xs)
 Unquotes and splices xs, only accessible in syntax-quote  

core/when  
---
macro 'core/when (2 &)  
nil  

core/when-not  
---
macro 'core/when-not (2 &)  
nil  

reader/read-char  
---
fn 'reader/read-char (1)  
(read-char parser)
 reads the next character from the input
 stream and returns it as an integer  

reader/read-val  
---
fn 'reader/read-val (1)  
(read-val parser) -> reads and parses the next value (values are
  numbers, strings, lists, ...)  

reader/reader-macro  
---
fn 'reader/reader-macro (2)  
(reader-macro character func) -> define a new reader-macro for
  character 'character. 'func is a fn taking two arguments:
  -> fn (parser next-character). reader-macros must exit using
  one of the 'rm-resp-* response fns. next-character is an int  

reader/rm-fail-eof  
---
fn 'reader/rm-fail-eof (1)  
(rm-fail-eof n) -> checks number 'n for stream EOF, throws
  an IllegalStateException if so. returns true  

reader/rm-resp-val  
---
fn 'reader/rm-resp-val (1)  
(rm-resp-val v) -> reader macro exit function telling the parser
  that it should return the value 'v  


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
