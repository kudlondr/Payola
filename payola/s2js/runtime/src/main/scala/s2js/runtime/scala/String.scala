package s2js.runtime.scala

import s2js.compiler.javascript

class String
{
    @javascript("return self.length;")
    def length = 0
}