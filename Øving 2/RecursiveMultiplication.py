import time
import tkinter as tk
import random

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


def plot_graph(times1, times2, test_cases):
    """
    Plots the time taken by the two multiplication methods.
    
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

    for i, n in enumerate(test_cases):
        x_pos = (i + 1) * canvas_width / (len(test_cases) + 1)

        y_pos1 = canvas_height - times1[i] * scaling_factor
        y_pos2 = canvas_height - times2[i] * scaling_factor

        canvas.create_oval(x_pos-3, y_pos1-3, x_pos+3, y_pos1+3, fill="blue", width=2)
        canvas.create_oval(x_pos-3, y_pos2-3, x_pos+3, y_pos2+3, fill="red", width=2)
        canvas.create_text(x_pos, canvas_height - 10, text=str(n), anchor=tk.S)

        if i > 0:
            prev_x_pos = (i) * canvas_width / (len(test_cases) + 1)
            prev_y_pos1 = canvas_height - times1[i-1] * scaling_factor
            prev_y_pos2 = canvas_height - times2[i-1] * scaling_factor
            canvas.create_line(prev_x_pos, prev_y_pos1, x_pos, y_pos1, fill="blue")
            canvas.create_line(prev_x_pos, prev_y_pos2, x_pos, y_pos2, fill="red")

        canvas.create_text(x_pos, y_pos1 - 10, text=f"{times1[i]:.0f} ns", anchor=tk.S)
        canvas.create_text(x_pos, y_pos2 - 20, text=f"{times2[i]:.0f} ns", anchor=tk.S)

    canvas.create_text(50, 20, text="Method 1", fill="blue")
    canvas.create_text(150, 20, text="Method 2", fill="red")

    root.mainloop()


if __name__ == "__main__":
    x = random.uniform(0, 10000)
    times1 = []
    times2 = []

    # Test cases
    test_cases = [5, 10, 25, 50, 75, 100, 150, 250, 300, 400, 500, 600, 700, 800,]
    for n in test_cases:
        test_multiplication(n, x, times1, times2)
    
    plot_graph(times1, times2, test_cases)
