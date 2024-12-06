import sympy # 如果报错显示没有这个包，就需要导入
from xeger import Xeger
import random
import subprocess
from subprocess import STDOUT, PIPE
from gendata import genData


def execute_java(stdin):
    cmd = ['java', '-jar', 'src.jar']# 更改为自己的.jar包名
    proc = subprocess.Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    modified_stdin = stdin;
    stdout, stderr = proc.communicate(stdin.encode())
    return stdout.decode().strip()


x = sympy.Symbol('x')
X = Xeger(limit=10)
cnt = 1

while True:
    cnt = cnt + 1
    if cnt % 1000 == 0:
        print(cnt)
    poly, ans = genData()
    print(poly)
    f = sympy.parse_expr(poly)
    poly2 = poly.replace('**', '^')
    print(poly2)
    strr = execute_java("0\n" + poly2)
    print(strr)
    strr = strr.replace('^', '**')
    print(strr)
    g = sympy.parse_expr(strr)
    if sympy.simplify(f).equals(g) :
        print("AC : " + str(cnt))
    else:
        print("!!WA!! with " + "poly : " + poly + " YOURS: " + strr)
