import matplotlib.pyplot as plt
import numpy as np
import math

#task 6.1.1
x = np.linspace(1, 10, 10)
y1 = x ** 0.5
y2 = x ** 1
y3 = x** 2

fig, (ax1, ax2) = plt.subplots(1,2, figsize= (12,6))

ax1.loglog(x, y1,label= 'x^0.5',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.loglog(x, y2,label= 'x^1',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.loglog(x, y3, label= 'x^2',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.set_xticks([2, 4, 6, 8, 10])
ax1.set_xticklabels([2, 4, 6, 8, 10])
ax1.set_yticks([1,10,100])
ax1.set_yticklabels([1,10,100])
ax1.set(title= 'logarithmic scaling x-axis and y-axis')
ax1.grid()
ax1.grid(which="minor", color="0.9")
ax1.legend()

ax2.plot(x, y1, label= 'x^0.5', linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.plot(x, y2, label= 'x^1',linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.plot(x, y3, label= 'x^2', linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.grid()
ax2.grid(which="minor", color="0.9")
ax2.set(title= 'linear')

ax2.legend()
fig.suptitle('Taks 6.1.1')
plt.show()

#task 6.1.2

x = np.linspace(1, 10, 10)
y1 = np.exp(0.5*x)
y2 = np.exp(1*x)
y3 = np.exp(2*x)

fig1, (ax1, ax2) = plt.subplots(1,2, figsize= (12,6))

ax1.semilogy(x, y1,label= 'e^0.5*X',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.semilogy(x, y2,label= 'e^1*x',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.semilogy(x, y3, label= 'e^2*x',linewidth=1, marker='o', markersize=4, linestyle='--')
#ax1.set_xticks([2, 4, 6, 8, 10])
#ax1.set_xticklabels([2, 4, 6, 8, 10])
#ax1.set_yticks([1,10,100])
#ax1.set_yticklabels([1,10,100])
ax1.set(title= 'linear scaling x-axis logarithmic scaling y-axis')
ax1.grid()
ax1.grid(which="minor", color="0.9")
ax1.legend()

ax2.plot(x, y1, label= 'e^0.5*x', linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.plot(x, y2, label= 'e^1*x',linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.plot(x, y3, label= 'e^2*x', linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.grid()
ax2.grid(which="minor", color="0.9")
ax2.set(title= 'linear')

ax2.legend()
fig.suptitle('Taks 6.1.2')
plt.show()

#task 6.1.3

x = np.linspace(1, 10, 10)
y1 = np.log(0.5*x)
y2 = np.log(1*x)
y3 = np.log(2*x)

fig1, (ax1, ax2) = plt.subplots(1,2, figsize= (12,6))

ax1.plot(x, y1,label= 'ln(0.5*X)',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.plot(x, y2,label= 'ln(1*x)',linewidth=1, marker='o', markersize=4, linestyle='--')
ax1.plot(x, y3, label= 'ln(2*x)',linewidth=1, marker='o', markersize=4, linestyle='--')
#ax1.set_xticks([2, 4, 6, 8, 10])
#ax1.set_xticklabels([2, 4, 6, 8, 10])
#ax1.set_yticks([1,10,100])
#ax1.set_yticklabels([1,10,100])
ax1.set(title= 'linear')
ax1.grid()
ax1.grid(which="minor", color="0.9")
ax1.legend()

ax2.semilogx(x, y1, label= 'ln(0.5*x)', linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.semilogx(x, y2, label= 'ln(1*x)',linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.semilogx(x, y3, label= 'ln(2*x)', linewidth=1, marker='o', markersize=4, linestyle='--')
ax2.grid()
ax2.grid(which="minor", color="0.9")
ax2.set(title= 'logarithmic scaling x-axis linear scaling y-axis')

ax2.legend()
fig.suptitle('Taks 6.1.3')
plt.show()
