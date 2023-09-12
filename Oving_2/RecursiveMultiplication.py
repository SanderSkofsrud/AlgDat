import time
import tkinter as tk
import random
import numpy as np

def recursive_multiplication1(n, x):
    """
    Multiplies a number using recursive summation.
    
    Parameters:
    - n: Number of times x should be added to itself.
    - x: The number to be multiplied.
    
    Returns:
    - Result of multiplying n with x using recursive summation.
    
    Time Complexity: Θ(n)
    """
    if n == 1:
        return x
    return x + recursive_multiplication1(n - 1, x)

def recursive_multiplication2(n, x):
    """
    Multiplies a number using a more efficient recursive method for even n values.
    
    Parameters:
    - n: Number of times x should be added to itself.
    - x: The number to be multiplied.
    
    Returns:
    - Result of multiplying n with x using a more efficient recursive method.
    
    Time Complexity: Θ(log n) for even n and Θ(n) for odd n.
    """
    if n == 1:
        return x
    if n % 2 == 0:
        return recursive_multiplication2(n // 2, x + x)
    else:
        return x + recursive_multiplication2(n - 1, x)

def test_multiplication(n, x, times1, times2):
    """
    Tests the performance of the two multiplication methods.
    
    Parameters:
    - n: The multiplier.
    - x: The multiplicand.
    - times1: List storing time results for method 1.
    - times2: List storing time results for method 2.
    """
    # Method 1
    start_time = time.perf_counter_ns()
    result1 = recursive_multiplication1(n, x)
    end_time = time.perf_counter_ns()
    time1 = end_time - start_time
    times1.append(time1)

    print(f"Method 1: {n} * {x:.2f} = {result1:.2f}")
    print(f"Time taken by method 1: {time1} nanoseconds\n")

    # Method 2
    start_time = time.perf_counter_ns()
    result2 = recursive_multiplication2(n, x)
    end_time = time.perf_counter_ns()
    time2 = end_time - start_time
    times2.append(time2)

    print(f"Method 2: {n} * {x:.2f} = {result2:.2f}")
    print(f"Time taken by method 2: {time2} nanoseconds\n")


def linear_regression(x, y):
    """
    Computes the slope and y-intercept of the best-fit line using linear regression.
    
    Parameters:
    - x: List of x values.
    - y: List of y values.
    
    Returns:
    - Slope and y-intercept of the best-fit line.
    """
    m, b = np.polyfit(x, y, 1)
    return m, b

def logarithmic_regression(x, y):
    """
    Computes the parameters of the best-fit logarithmic curve.
    
    Parameters:
    - x: List of x values.
    - y: List of y values.
    
    Returns:
    - Parameters a and b of the best-fit curve y = a + b * log(x).
    """
    log_x = np.log(x)
    b, a = np.polyfit(log_x, y, 1)
    return a, b

def plot_graph(times1, times2, test_cases):
    """
    Plots the regression lines for the two multiplication methods.
    
    Parameters:
    - times1: List of time results for method 1.
    - times2: List of time results for method 2.
    - test_cases: List of n values for which methods were tested.
    """
    root = tk.Tk()
    root.title("Performance Comparison")

    canvas_width = 800
    canvas_height = 400
    canvas = tk.Canvas(root, width=canvas_width, height=canvas_height)
    canvas.pack()

    max_time = max(max(times1), max(times2))
    scaling_factor = (canvas_height - 50) / max_time

    # Compute regression lines for Method 1 (linear)
    m1, b1 = linear_regression(test_cases, times1)

    # Compute regression lines for Method 2 (logarithmic)
    a2, b2 = logarithmic_regression(test_cases, times2)

    for i in range(len(test_cases)-1):
        x1 = (i + 1) * canvas_width / (len(test_cases) + 1)
        x2 = (i + 2) * canvas_width / (len(test_cases) + 1)

        y1_method1 = canvas_height - (m1 * test_cases[i] + b1) * scaling_factor
        y2_method1 = canvas_height - (m1 * test_cases[i+1] + b1) * scaling_factor

        y1_method2 = canvas_height - (a2 + b2 * np.log(test_cases[i])) * scaling_factor
        y2_method2 = canvas_height - (a2 + b2 * np.log(test_cases[i+1])) * scaling_factor

        canvas.create_line(x1, y1_method1, x2, y2_method1, fill="blue")
        canvas.create_line(x1, y1_method2, x2, y2_method2, fill="red")

    canvas.create_text(50, 20, text="Method 1 (Linear)", fill="blue")
    canvas.create_text(200, 20, text="Method 2 (Logarithmic)", fill="red")

    root.mainloop()


if __name__ == "__main__":
    x = random.uniform(0, 10000)
    times1 = []
    times2 = []

    # Test cases
    test_cases = [i for i in range(1, 100)]
    for n in test_cases:
        test_multiplication(n, x, times1, times2)
    
    plot_graph(times1, times2, test_cases)
