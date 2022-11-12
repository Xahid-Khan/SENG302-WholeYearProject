"""
Short python script that saves the values of environment variables in the current environment into 'runner_env.sh'
These environment variables can then be re-exported when necessary by running 'source runner_env.sh'
"""
import os

ENVIRONMENT_VARIABLES_NAMES = [
    "DB_PASSWORD",
    "SONARQUBE_KEY",
]

env_variable_values = []

# Read them from the current environment
for env_variable_name in ENVIRONMENT_VARIABLES_NAMES:
    try:
        env_variable_values.append(os.environ[env_variable_name])
    except KeyError as ex:
        print("Failed to find required environment variable with name {}".format(env_variable_name))
        exit(1)


# Save them to `runner_env.sh`
with open("runner_env.sh", 'w') as f:
    for name, value in zip(ENVIRONMENT_VARIABLES_NAMES, env_variable_values):
        f.write('export {}="{}"\n'.format(name, value.replace('"', r'\"')))
