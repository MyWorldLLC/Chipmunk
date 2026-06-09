local module = {}
local assert = require('./assert')

function module.main()
    return assert.equal('Hello, World!', 'Hello, World!', 'Expected a cheerful greeting.')
end

return module