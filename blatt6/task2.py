import pandas as pd
import matplotlib.pyplot as plt

# Read the CSV file into a DataFrame
df = pd.read_csv('blatt6/US_States_Production.csv')

df.head()  # Display the first few rows of the DataFrame

# Set labels for the dataframe 12 columns
df.columns = ['obsNr', 'state', 'year', 'private capital', 'highway and street capital', 'water utility capital', 'utility capital', 'public capital', 'gross state product', 'employment', 'unemployment rate']

df.info()  # Display information about the DataFrame

# Unemployment rate between 1970 and 1986 in the United States. We need to average the unemployment rate for each year across all states.
# Group the DataFrame by 'year' and calculate the mean unemployment rate for each year
unemployment_by_year = df.groupby('year')['unemployment rate'].mean()

# Plot the average unemployment rate over the years
plt.figure(figsize=(10, 6))
plt.plot(unemployment_by_year.index, unemployment_by_year.values, marker='o')
plt.title('Average Unemployment Rate in the United States (1970-1986)')
plt.xlabel('Year')
plt.ylabel('Average Unemployment Rate (%)')
plt.grid()


# Private capital between 1970 and 1986 for the five states having the highest private capital in average.
private_capital_by_state = df.groupby('state')['private capital']
mean_private_capital = private_capital_by_state.mean().sort_values(ascending=False).head(5)
print(mean_private_capital)
print(mean_private_capital.index)  # Print the names of the top 5 states

print(private_capital_by_state)  # Print the private capital by state

# Plot the private capital for the top 5 states from 1970 to 1986
plt.figure(figsize=(10, 6))
for state in mean_private_capital.index:
    state_data = df[df['state'] == state]
    plt.plot(state_data['year'], state_data['private capital'], marker='o', label=state)

plt.title('Private Capital in the Top 5 States (1970-1986)')
plt.xlabel('Year')
plt.ylabel('Private Capital')
plt.legend()
plt.grid()


# Public capital between 1970 and 1986 for the state having the highest and the state having the lowest public capital in average.
public_capital_by_state = df.groupby('state')['public capital']
mean_public_capital = public_capital_by_state.mean()
highest_public_capital_state = mean_public_capital.idxmax()
lowest_public_capital_state = mean_public_capital.idxmin()
print(f"State with the highest average public capital: {highest_public_capital_state}")
print(f"State with the lowest average public capital: {lowest_public_capital_state}")

# Plot the public capital for the state with the highest and the state with the lowest public capital from 1970 to 1986
plt.figure(figsize=(10, 6))
highest_state_data = df[df['state'] == highest_public_capital_state]
lowest_state_data = df[df['state'] == lowest_public_capital_state]
plt.plot(highest_state_data['year'], highest_state_data['public capital'], marker='o', label=f"High: {highest_public_capital_state}")
plt.plot(lowest_state_data['year'], lowest_state_data['public capital'], marker='s', label=f"Low: {lowest_public_capital_state}")
plt.title('Public Capital in the Highest and Lowest States (1970-1986)')
plt.xlabel('Year')
plt.ylabel('Public Capital')
plt.legend()
plt.grid()



# Boxplots
# Unemployment rate between 1970 and 1986 for the two states having the highest
# unemployment rate on average
unemployment_by_state = df.groupby('state')['unemployment rate']
top_two_unemployment_by_state = unemployment_by_state.mean().sort_values(ascending=False).head(2)
print(top_two_unemployment_by_state)
    
# Create a boxplot for the unemployment rate of the two states
plt.figure(figsize=(10, 6))
for state in top_two_unemployment_by_state.index:
    state_data = df[df['state'] == state]
    plt.boxplot(state_data['unemployment rate'], positions=[list(top_two_unemployment_by_state.index).index(state) + 1], label=state)

plt.title('Unemployment Rate in the Top 2 States (1970-1986)')
plt.xlabel('State')
plt.ylabel('Unemployment Rate (%)')
plt.xticks(
    range(1, len(top_two_unemployment_by_state) + 1),
    top_two_unemployment_by_state.index,
)
plt.grid()


# Highway and street capital between 1970 and 1986 for the two states having the
# lowest highway and street capital on average.
highway_street_capital_by_state = df.groupby('state')['highway and street capital']
lowest_two_highway_street_capital_by_state = highway_street_capital_by_state.mean().sort_values(ascending=True).head(2)
print(lowest_two_highway_street_capital_by_state)

# Create a boxplot for the highway and street capital of the two states
plt.figure(figsize=(10, 6))
for state in lowest_two_highway_street_capital_by_state.index:
    state_data = df[df['state'] == state]
    plt.boxplot(state_data['highway and street capital'], positions=[list(lowest_two_highway_street_capital_by_state.index).index(state) + 1], label=state)

plt.title('Highway and Street Capital in the Lowest 2 States (1970-1986)')
plt.xlabel('State')
plt.ylabel('Highway and Street Capital')
plt.xticks(
    range(1, len(lowest_two_highway_street_capital_by_state) + 1),
    lowest_two_highway_street_capital_by_state.index,
)
plt.grid()

# Display the three plots together in separate figures.
plt.show()