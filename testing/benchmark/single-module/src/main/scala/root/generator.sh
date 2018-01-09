#!/usr/bin/env bash

#define the template.
begin=1
end=99

mkdir -p nested

cat << EOF > MainClass.scala
package root
EOF

for i in $(seq $begin $end);do
cat << EOF >> MainClass.scala
import nested.NestedClass$i
EOF
done

cat << EOF >> MainClass.scala
object MainClass {

  def method1(): Unit = {

    implicit val doesntMatter: Int = 0

    val objects: Array[Printable] = Array(
EOF

end2=$((end-1))


for i in $(seq $begin $end2);do
cat << EOF >> MainClass.scala
new NestedClass$i(),
EOF
done
echo "      new NestedClass$end()" >> MainClass.scala
cat << EOF >> MainClass.scala
    )
    for(instance <- objects){
      instance.print()
    }
  }
}
EOF


generateA(){
for i in $(seq $begin $end);do
cat << EOF > nested/NestedClass$i.scala
package root.nested

import root.Printable

class NestedClass$i(implicit val doesntMatter: Int) extends Printable{

  private val j = $i

  def print(): Unit = {
    println(s"Class no. \$j")
  }

}
EOF
done
}

generateB(){
let end="begin + 1"
for i in $(seq $begin $end);do
cat << EOF > nested/NestedClass$i.scala
package root.nested

import root.Printable

class NestedClass$i(implicit val doesntMatter: Int) extends Printable{

  private val j = $i
  private val change = $1

  def print(): Unit = {
    println(s"Class no. \$j")
  }

}
EOF
done
}

let param=$1

if [[ param == "0" ]];
then
   generateA
else
   generateB $param
fi




