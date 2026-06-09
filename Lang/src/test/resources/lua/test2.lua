local module = {}
local assert = require('./assert')

function module.main()
    return assert.equal('Hello, World from test 2!', 'Hello, World from test 2!', 'Expected a cheerful greeting from test 2.')
end

return module