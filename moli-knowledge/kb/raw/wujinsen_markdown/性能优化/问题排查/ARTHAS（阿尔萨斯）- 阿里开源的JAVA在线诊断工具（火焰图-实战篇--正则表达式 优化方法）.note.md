htps:/ w.fresion.com/article/72764305/

# 背景 ⽬前 分析

因为耗时主要耗在compile编译阶段 这个阶段，在外⾯还是⼀个通过乐观锁进⾏竞争的，根据matcher的matcher⽅法

privatevoidcompile(){ / Handle canonical equivalences if(has(CANON_EQ) &!has(LITERAL ){

normalize(); }else{

normalizedPattern =pattern;

} patternLength =normalizedPattern.length();

/ Copy patern to int aray for convenience / Use double zero to terminate patern

temp =newint[patternLength +2];

hasSupplementary =false; intc,count =0;

/ Convert al chars into code points

for(intx =0;x <patternLength;x +=Character.charCount(c ){ c =normalizedPattern.codePointAt(x); if(isSuplementary(c ){

hasSupplementary =true;

} temp[count +]=c;

}

patternLength =count; / paternLength now in code points

if(!has(LITERAL ) RemoveQEQuoting();

/ Alocate al temporary objects here. buffer =newint[32]; groupNodes =newGroupHead[10]; namedGroups =null;

if(has(LITERAL ){

/ Literal patern handling matchRoot =newSlice(temp,patternLength,hasSupplementary); matchRoot.next =lastAccept;

}else{

/ Start recursive descent parsing matchRoot =expr(lastAccept);

/ Check extra patern characters if(patternLength !=cursor){

if(pek() =')'){

throweror("Unmatched closing ')'"); }else{

throweror("Unexpected internal eror"); }

} }

/ Pephole optimization

if(matchRoot instanceofSlice){ root =BnM.optimize(matchRoot); if(root =matchRoot){

root =hasSupplementary ?newStartS(matchRoot):newStart(matchRoot); }

}elseif(matchRoot instanceofBegin| matchRoot instanceofFirst){

root =matchRoot; }else{

root =hasSupplementary ?newStartS(matchRoot):newStart(matchRoot); }

/ Release temporary storage temp =null; buffer =null; groupNodes =null; patternLength =0; compiled =true;

}

# 优化

所以，通过预先编译，将上⾯的comiled字段置为true，在使⽤的时候，就不⽤每次这么负责的编译 了。 使⽤的时候，直接

