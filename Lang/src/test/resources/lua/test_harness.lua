
local suites = {
    ['Test 1'] = './test',
    ['Test 2'] = './test2'
}

local results = {}
local failures = false

for suiteName, suite in suites do
    local module = require(suite)
    for testName, testFn in module do
        local result = testFn()
        if result.failed then
            print(suiteName .. '.' .. testName .. ': ' .. result.message)
            failures = true
        end
    end
end

if failures then
    error("Some tests had failures")
end

